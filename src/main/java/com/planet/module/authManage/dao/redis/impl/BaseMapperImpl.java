package com.planet.module.authManage.dao.redis.impl;

import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.util.jdk8.ObjectSerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Description：实现redis的crud的工具类
 */
@Component
@Slf4j
public class BaseMapperImpl implements BaseMapper {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void creatCache(String cacheName, Object cacheObj, Long millisecond) {
        RBucket<String> bucket = redissonClient.getBucket(cacheName);
        try {
            bucket.trySet(ObjectSerializeUtil.objToSerialize(cacheObj), millisecond/1000, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("序列化失败:对象结构错误",e);
        }
    }

    @Override
    public Object getCache(String cacheName) {
        RBucket<String> bucket = redissonClient.getBucket(cacheName);
        Object obj = null;
        String str = bucket.get();
        if(str!=null&&!"".equals(str)){
            try {
                obj = ObjectSerializeUtil.serializeToObj(str);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("反序列化失败",e);
            }
        }
        return obj;
    }

    @Override
    public void removeCache(String cacheName) {
        RBucket<String> bucket = redissonClient.getBucket(cacheName);
        bucket.delete();
    }

    @Override
    public void updateCache(String cacheName, Object cacheObj, Long millisecond) {
        RBucket<String> bucket = redissonClient.getBucket(cacheName);
        try {
            bucket.set(ObjectSerializeUtil.objToSerialize(cacheObj), millisecond/1000, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("序列化失败",e);
        }
    }
}
