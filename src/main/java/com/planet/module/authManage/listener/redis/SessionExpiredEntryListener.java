package com.planet.module.authManage.listener.redis;

import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.authByShiro.UserShiroService;
import com.planet.util.jdk8.ObjectSerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 对存在redis中的session在失效时进行监听的Listener
 */
@Component
@Slf4j
public class SessionExpiredEntryListener implements EntryExpiredListener<String, String> {
    @Autowired
    private UserShiroService userShiroService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private AccountLogMapper accountLogMapper;


    @Override
    public void onExpired(EntryEvent<String, String> event) {
        log.info("session失效,调用onExpired方法进行用户登出记录..");
        String sessionIdKey=event.getKey();
        Long userId=null;
        try {
            Session session = (Session)ObjectSerializeUtil.serializeToObj(event.getValue());
            Object o = session.getAttribute("userId");
            if(o==null){
                log.error("获取失效session中的userId失败:userId为空");
                return;
            }
            userId=(Long)o;
        } catch (IOException |ClassNotFoundException e) {
            e.printStackTrace();
            log.error("获取失效session中的userId失败:出现异常");
        }
        //1.删除此session对应的deque
        userShiroService.deleteUserSessionByUserId(userId);
        //2.进行用户操作log记录
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String name=null;
        if(userInfo!=null){
            name=userInfo.getName();
        }
        AccountLog accountLog=new AccountLog();
        accountLog.setUserId(userId);
        accountLog.setName(name);
        accountLog.setMethod("onExpired");
        accountLog.setContent("("+sessionIdKey+")("+userId+")("+name+")");
        int insert = accountLogMapper.insert(accountLog);
        if(insert>0){
            log.info("session失效,调用onExpired方法进行用户登出记录>>>成功");
        }else{
            log.error("session失效,调用onExpired方法进行用户登出记录>>>失败");
        }
    }

}
