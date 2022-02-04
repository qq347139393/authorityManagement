package com.planet.system.authByShiro.customSettings;

import com.planet.common.constant.UtilsConstant;
import com.planet.util.jdk8.ObjectSerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @Description：自定义统一sessiondao实现
 */
@Slf4j
public class ShiroRedisSessionDao  extends AbstractSessionDAO {
    @Autowired
    private RedissonClient redissonClient;
    /**
     * @Description 创建session
     * @param session 会话对象
     * @return
     */
    @Override
    protected Serializable doCreate(Session session) {
        //创建唯一标识的sessionId
        Serializable sessionId = generateSessionId(session);
        //为session会话指定唯一的sessionId
        assignSessionId(session, sessionId);
        //放入缓存中
        String key = UtilsConstant.SESSION_KEY+sessionId.toString();
        RBucket<String> bucket = redissonClient.getBucket(key);
        try {
            bucket.trySet(ObjectSerializeUtil.objToSerialize(session), UtilsConstant.TTL_SESSION_MILLISECOND/1000, TimeUnit.SECONDS);
            return sessionId;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("创建session对象后存入redis失败",e);
            return null;
        }
    }

    /**
     * @Description 读取session
     * @param sessionId 唯一标识
     * @return
     */
    @Override
    protected Session doReadSession(Serializable sessionId) {
        String key = UtilsConstant.SESSION_KEY+sessionId.toString();
        RBucket<String> bucket = redissonClient.getBucket(key);
        Object obj = null;
        String str = bucket.get();
        if(str!=null&&!"".equals(str)){
            try {
                obj = ObjectSerializeUtil.serializeToObj(str);
                return (Session) obj;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("反序列化失败",e);
            }
        }
        return null;//获取失败,返回null
    }

    /**
     * @Description 更新session
     * @param session 对象
     * @return
     */
    @Override
    public void update(Session session) throws UnknownSessionException {
        String key = UtilsConstant.SESSION_KEY+session.getId().toString();
        RBucket<String> bucket = redissonClient.getBucket(key);
        try {
            bucket.set(ObjectSerializeUtil.objToSerialize(session), UtilsConstant.TTL_SESSION_MILLISECOND/1000, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("创建session对象后存入redis失败",e);
        }
    }

    /**
     * @Description 删除session
     * @param
     * @return
     */
    @Override
    public void delete(Session session) {
        String key = UtilsConstant.SESSION_KEY+session.getId().toString();
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return null;
    }
}
