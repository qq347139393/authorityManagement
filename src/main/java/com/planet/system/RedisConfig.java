package com.planet.system;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:config/redis.yml")
@Data
public class RedisConfig {
    @Autowired
    private Environment env;

//    @Bean(name = "redisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
//
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        ////参照StringRedisTemplate内部实现指定序列化器
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        //使用Jackson序列化器
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//        return redisTemplate;
//    }

    @Bean("redissonClient")
    public RedissonClient redissonClient(){//与shiro无关
        // 单机redis配置
        Config config = new Config();
        config.useSingleServer().setAddress(env.getProperty("url"))
                .setPassword(env.getProperty("password"))
                .setConnectTimeout(Integer.valueOf(env.getProperty("connect-timeout")))
                .setConnectionMinimumIdleSize(Integer.valueOf(env.getProperty("connection-minimumidle-size")))
                .setConnectionPoolSize(Integer.valueOf(env.getProperty("connect-pool-size")))
                .setTimeout(Integer.valueOf(env.getProperty("timeout")));
        //创建redission的客户端，交于spring管理
        RedissonClient client = Redisson.create(config);
        return client;
    }

//    private RedisSerializer<String> keySerializer(){
//        return new StringRedisSerializer();
//    }

//    //使用Jackson序列化器
//    private RedisSerializer<Object> valueSerializer(){
//        return new GenericJackson2JsonRedisSerializer();
//    }
}

