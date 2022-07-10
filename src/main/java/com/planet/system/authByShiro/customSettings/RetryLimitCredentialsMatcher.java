package com.planet.system.authByShiro.customSettings;

import com.planet.common.constant.ComponentConstant;
import com.planet.common.constant.LocalCacheConstantService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Description：自定义密码比较器
 */
//@Component("retryLimitCredentialsMatcher")
public class RetryLimitCredentialsMatcher extends HashedCredentialsMatcher {
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 重写后,会限制在一定时间内的密码输入错误的次数
     * @param token
     * @param info
     * @return
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        //0.构建key
        Long userId = ((CustomUserToken)token).getUserId();
        String userLoginFrequency=ComponentConstant.USER_LOGIN_FREQUENCY+userId;
        // 1、获取系统中是否已有登录次数缓存,缓存对象结构预期为："用户名--登录次数"。
        RAtomicLong atomicLong = redissonClient.getAtomicLong(userLoginFrequency);
        //2、如果之前没有登录缓存，则创建一个登录次数缓存。
        long retryFlat = atomicLong.get();
        Long retryLimitNum=LocalCacheConstantService.getValue("account:retryLimitNum",Long.class);
        //判断是否超过次数
        if (retryFlat> retryLimitNum){
            //3、如果缓存次数已经超过限制，则驳回本次登录请求。
            Long retryLimitExceedWaitTime=LocalCacheConstantService.getValue("account:retryLimitExceedWaitTime",Long.class);
            atomicLong.expire(retryLimitExceedWaitTime, TimeUnit.MINUTES);
            throw new ExcessiveAttemptsException("密码输入错误次数超过了"+(retryLimitNum+1)+"次,请等待"+retryLimitExceedWaitTime+"分钟后重试..");
        }
        //4、将缓存记录的登录次数加1,设置指定时间内有效
        atomicLong.incrementAndGet();
        atomicLong.expire(LocalCacheConstantService.getValue("account:retryLimitExceedWaitTime",Long.class), TimeUnit.MINUTES);
        //5、验证用户本次输入的帐号密码，如果登录登录成功，则清除掉登录次数的缓存
        boolean flag = super.doCredentialsMatch(token, info);
        if (flag){
            atomicLong.delete();
        }
        return flag;
    }
}
