package com.planet.system.authByShiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.ComponentConstant;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.SuperConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mysql.mapper.FunctionInfoMapper;
import com.planet.module.authManage.dao.redis.authByShiro.ShiroRedisSessionDao;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.system.authByShiro.customSettings.*;
import com.planet.system.authByShiro.filter.JwtAuthcFilter;
import com.planet.system.authByShiro.filter.RestAuthorizationFilter;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shiro认证和鉴权的配置类
 */
@Configuration
@ComponentScan(basePackages = "com.planet")
//@EnableConfigurationProperties({ShiroRedisProperties.class})
@Log4j2
public class ShiroConfig {
    @Autowired
    private FunctionInfoMapper functionInfoMapper;


    /**
     * @Description 创建cookie对象
     * 这个创建的是返回给客户端的sessionId对应的key值,通常存入浏览器的cookie里(如果用vue可以放到store里)
     */
    @Bean(name="simpleCookie")
    public SimpleCookie simpleCookie(){
        SimpleCookie simpleCookie = new SimpleCookie();
        //这个设置的是返回给客户端的sessionId对应的key值
        simpleCookie.setName("shiroSession");
        return simpleCookie;
    }

    /**
     * @Description 权限管理器
     * @param
     * @return
     */
    @Bean(name="defaultWebSecurityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(@Qualifier("shiroCustomRealm") ShiroCustomRealm shiroCustomRealm){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroCustomRealm);
        securityManager.setSessionManager(shiroSessionManager());
        return securityManager;
    }

    /**
     * @Description 自定义Realm
     */
    @Bean(name="shiroCustomRealm")
    public ShiroCustomRealm authShiroRealm(@Qualifier("retryLimitCredentialsMatcher") RetryLimitCredentialsMatcher retryLimitCredentialsMatcher){
        ShiroCustomRealm shiroCustomRealm = new ShiroCustomRealm();
        shiroCustomRealm.setCredentialsMatcher(retryLimitCredentialsMatcher);
        return shiroCustomRealm;
    }

    /**
     * 注册自定义密码比较器
     * @return
     */
    @Bean(name="retryLimitCredentialsMatcher")
    public RetryLimitCredentialsMatcher retryLimitCredentialsMatcher(){
        RetryLimitCredentialsMatcher retryLimitCredentialsMatcher=new RetryLimitCredentialsMatcher();
        retryLimitCredentialsMatcher.setHashAlgorithmName(SuperConstant.HASH_ALGORITHM);
        retryLimitCredentialsMatcher.setHashIterations(SuperConstant.HASH_INTERATIONS);
        return retryLimitCredentialsMatcher;
    }


    /**
     * 自定义RedisSessionDao
     * @return
     */
    @Bean("redisSessionDao")
    public ShiroRedisSessionDao redisSessionDao(){
        return new ShiroRedisSessionDao();
    }

    /**
     * @Description 自定义会话管理器
     */
    @Bean(name="sessionManager")
    public DefaultWebSessionManager shiroSessionManager(){
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setSessionDAO(redisSessionDao());
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionIdCookie(simpleCookie());
        sessionManager.setGlobalSessionTimeout(LocalCacheConstantService.getValue("redis:ttlSessionMillisecond",Long.class));
        return sessionManager;
    }

    /**
     * @Description 自定义拦截器定义
     */
    private Map<String, Filter> filters() {
        Map<String, Filter> map = new HashMap<String, Filter>();
//        map.put("role-or", new RolesOrAuthorizationFilter());
//        map.put("kicked-out", new KickedOutAuthorizationFilter(redissonClient(), redisSessionDao(), shiroSessionManager()));
        map.put("jwt-authc", new JwtAuthcFilter());//鉴证过滤器
//        map.put("rest-perms", new RestAuthorizationFilter());//restful鉴权过滤器
//        map.put("jwt-perms", new JwtPermsFilter());
//        map.put("jwt-roles", new JwtRolesFilter());
        return map;
    }

    //权限链
    public Map<String, String> loadFilterChainDefinition() {
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
        //放心静态文件
        map.put("/file/**","anon");

//        List<FunctionInfo> functionInfos = functionInfoMapper.selectList(new QueryWrapper<FunctionInfo>().isNotNull("url"));
//        functionInfos.stream().forEach(functionInfo -> {//遍历需要[鉴权]的接口
//            map.put(functionInfo.getUrl(), "rest-perms["+functionInfo.getPermit()+"]");
//        });

//        map.put("/authManage/test","anon");
        map.put("/**","jwt-authc");//余下的请求都要进行[鉴证]
//        map.put("/**","anon");
//        HttpMethodPermissionFilter
        return map;
    }

    /**
     * @Description Shiro过滤器
     */
    @Bean("restShiroFilterFactoryBean")
    public RestShiroFilterFactoryBean restShiroFilterFactoryBean(@Qualifier("defaultWebSecurityManager") DefaultWebSecurityManager defaultWebSecurityManager) {
        RestShiroFilterFactoryBean restShiroFilterFactoryBean = new RestShiroFilterFactoryBean();
        restShiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);
        //使自定义拦截器生效
        restShiroFilterFactoryBean.setFilters(filters());
        restShiroFilterFactoryBean.setFilterChainDefinitionMap(loadFilterChainDefinition());
        restShiroFilterFactoryBean.setLoginUrl("/login");
        restShiroFilterFactoryBean.setUnauthorizedUrl("/login");
        return restShiroFilterFactoryBean;
    }


}
