package com.planet.module.authManage.service.authByShiro.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.ComponentConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mysql.mapper.FunctionInfoMapper;
import com.planet.module.authManage.dao.redis.authByShiro.ShiroRedisSessionDao;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.service.FunctionInfoService;
import com.planet.module.authManage.service.authByShiro.ShiroService;
import com.planet.system.authByShiro.customSettings.RestPathMatchingFilterChainResolver;
import com.planet.system.authByShiro.customSettings.RestShiroFilterFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShiroServiceImpl implements ShiroService {
    @Autowired
    private FunctionInfoMapper functionInfoMapper;
    @Autowired
    private DefaultWebSessionManager defaultWebSessionManager;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ShiroRedisSessionDao shiroRedisSessionDao;
    @Autowired
    private RestShiroFilterFactoryBean restShiroFilterFactoryBean;

    public void userSessionManage(Long userId,String sessionId){
        //用于限制用户并发登录次数
        //a1:从redis缓存中获取当前userId对应的user:sessionId队列
        RDeque<String> deque = redissonClient.getDeque(UtilsConstant.USER_SESSION_ID  + userId);
        //a2:判断是否存在当前sessionId
        boolean flag = deque.contains(sessionId);
        //a3:不存在则放入队列尾端==>存入sessionId
        if (!flag){
            deque.addLast(sessionId);
        }
        //a4:判断当前队列大小是否超过限定此账号的可在线人数,如果超过了就要将前一个sessionId和session删除,以实现踢人的效果
        if (deque.size()>1){
            String oldSessionId = deque.getFirst();
            deque.removeFirst();
            Session session = null;
            try {
                session = defaultWebSessionManager.getSession(new DefaultSessionKey(oldSessionId));
            }catch (UnknownSessionException ex){
                log.info("session已经失效");
            }catch (ExpiredSessionException expiredSessionException){
                log.info("session已经过期");
            }
            if (session != null && !"".equals(session)){
                shiroRedisSessionDao.delete(session);
            }
        }
        //a5:将userId存入新的session中-->这是为了后面sessionId失效或被销毁时在触发的监听事件里通过session来获取userId,从而可以进行用户登录记录
        Session session = defaultWebSessionManager.getSession(new DefaultSessionKey(sessionId));
        session.setAttribute("userId",userId);
    }

    @Override
    public void deleteUserSessionByUserId(Long userId) {
        //a1:从redis缓存中获取当前userId对应的user:sessionId队列
        RDeque<String> deque = redissonClient.getDeque(UtilsConstant.USER_SESSION_ID  + userId);
        //a2:删除当前userId对应的队列
        deque.delete();
    }

    @Override
    public boolean updateShiroPermissions() {
        synchronized (this) {
            AbstractShiroFilter shiroFilter;
            try {
                shiroFilter = (AbstractShiroFilter) restShiroFilterFactoryBean.getObject();
            } catch (Exception e) {
                throw new RuntimeException("get ShiroFilter from shiroFilterFactoryBean error!");
            }
            RestPathMatchingFilterChainResolver restPathMatchingFilterChainResolver = (RestPathMatchingFilterChainResolver) shiroFilter.getFilterChainResolver();
            DefaultFilterChainManager manager = (DefaultFilterChainManager) restPathMatchingFilterChainResolver.getFilterChainManager();

            // 清空拦截管理器中的存储
            manager.getFilterChains().clear();
            // 清空拦截工厂中的存储,如果不清空这里,还会把之前的带进去
            //            ps:如果仅仅是更新的话,可以根据这里的 map 遍历数据修改,重新整理好权限再一起添加
            restShiroFilterFactoryBean.getFilterChainDefinitionMap().clear();
            // 动态查询数据库中所有权限

            //这里的map需要有序map
            Map<String, String> map = new LinkedHashMap<>();
            map.put("/authManage/account-module/login","anon");//登录放行
            map.put("/authManage/account-module/isVeriCodeByPic","anon");//是否启用验证码放行
            map.put("/authManage/account-module/veriCodeByPic","anon");//获取验证码放行
            //swagger相关的测试接口放行
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/swagger-ui.html", "anon");
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/webjars/**", "anon");
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/swagger-resources", "anon");
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/swagger-resources/**", "anon");
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/v2/**", "anon");
            map.put(ComponentConstant.SWAGGER2_DEFAULT_PATH+"/csrf", "anon");

            List<FunctionInfo> functionInfos = functionInfoMapper.selectList(new QueryWrapper<FunctionInfo>().isNotNull("url"));
            functionInfos.stream().forEach(functionInfo -> {//遍历需要[鉴权]的接口
                map.put(functionInfo.getUrl(), "rest-perms["+functionInfo.getPermit()+"]");
            });

//        map.put("/authManage/test","anon");
            map.put("/**","jwt-authc");//余下的请求都要进行[鉴证]
//        map.put("/**","anon");
//        HttpMethodPermissionFilter

            restShiroFilterFactoryBean.setFilterChainDefinitionMap(map);
            // 重新构建生成拦截
            Map<String, String> chains = restShiroFilterFactoryBean.getFilterChainDefinitionMap();
            for (Map.Entry<String, String> entry : chains.entrySet()) {
                manager.createChain(entry.getKey(), entry.getValue());
            }
            log.info("--------------- 动态更新url权限成功！ ---------------");
            return true;
        }
    }
}
