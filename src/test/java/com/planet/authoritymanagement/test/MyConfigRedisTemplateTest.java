//package com.planet.authoritymanagement.test;
//
//import lombok.Data;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.BoundListOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class MyConfigRedisTemplateTest {
//    //在MyRedisConfig文件中配置了redisTemplate的序列化之后， 客户端也能正确显示键值对了
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Test
//    public void test(){
//        redisTemplate.opsForValue().set("wujinxing", "lige");
//        System.out.println(redisTemplate.opsForValue().get("wujinxing"));
//        Map<String, Object> map = new HashMap<>();
//        for (int i=0; i<10; i++){
//            User user = new User();
//            user.setId(i);
//            user.setName(String.format("测试%d", i));
//            user.setAge(i+10);
//            map.put(String.valueOf(i),user);
//        }
//        redisTemplate.opsForHash().putAll("测试", map);
//        BoundHashOperations hashOps = redisTemplate.boundHashOps("测试");
//        Map map1 = hashOps.entries();
//
//        System.out.println(redisTemplate.opsForList().leftPush("1", "1"));
//        System.out.println(redisTemplate.opsForList().leftPush("1", "2"));
//        System.out.println(redisTemplate.opsForList().leftPush("1", "3"));
//        Object index = redisTemplate.opsForList().index("1", 0);
////        Object o = redisTemplate.opsForList().leftPop("1");
//        List range = redisTemplate.opsForList().range("1", 0, 1000);
//    }
//    @Data
//    static class User implements Serializable {
//        private int id;
//        private String name;
//        private long age;
//
//    }
//}
