package com.planet.system.authByShiro.service.impl;

import com.planet.common.constant.UtilsConstant;
import com.planet.system.authByShiro.customSettings.ShiroRedisSessionDao;
import com.planet.system.authByShiro.service.UserShiroService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserShiroServiceImpl implements UserShiroService {
    @Autowired
    private DefaultWebSessionManager defaultWebSessionManager;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ShiroRedisSessionDao shiroRedisSessionDao;

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
            sessionId = deque.getFirst();
            deque.removeFirst();
            Session session = null;
            try {
                session = defaultWebSessionManager.getSession(new DefaultSessionKey(sessionId));
            }catch (UnknownSessionException ex){
                log.info("session已经失效");
            }catch (ExpiredSessionException expiredSessionException){
                log.info("session已经过期");
            }
            if (session != null && !"".equals(session)){
                shiroRedisSessionDao.delete(session);
            }
        }
    }

    @Override
    public void deleteUserSessionByUserId(Long userId) {
        //a1:从redis缓存中获取当前userId对应的user:sessionId队列
        RDeque<String> deque = redissonClient.getDeque(UtilsConstant.USER_SESSION_ID  + userId);
        //a2:删除当前userId对应的队列
        deque.delete();
    }
}
