package com.planet.authoritymanagement.test;

import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.entity.redis.UserRoleRs;
import com.planet.module.authManage.service.AccountModuleService;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.planet.module.authManage.dao.mapper")//加上你项目的dao或service所在文件位置即可
public class Redis2TemplateTest {
    @Autowired
    private AccountModuleService accountModuleService;

    @Test
    public void test1(){
        UserInfo userInfo = accountModuleService.selectUserByUserId(1l);
        System.out.println(userInfo);
        UserRoleRs userRoleRs = accountModuleService.selectRolesByUserId(1l);
        System.out.println(userRoleRs);
        UserFunctionRs userFunctionRs = accountModuleService.selectFunctionsByUserId(1l);
        System.out.println(userFunctionRs);

    }

}
