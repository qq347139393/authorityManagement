package com.planet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.planet.module.authManage.dao.mysql.mapper")//加上你项目的dao或service所在文件位置即可
public class AuthorityManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorityManagementApplication.class, args);
    }

}
