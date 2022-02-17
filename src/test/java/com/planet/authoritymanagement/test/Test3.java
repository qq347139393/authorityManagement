package com.planet.authoritymanagement.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.planet.module.authManage.entity.mysql.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test3 {

    public static void main(String[] args) {

        // 生成指定url对应的二维码到文件，宽和高都是300像素
        QrCodeUtil.generate("https://hutool.cn/", 300, 300, FileUtil.file("D:/home/user/qrcode.jpg"));
        String decode = QrCodeUtil.decode(FileUtil.file("D:/home/user/qrcode.jpg"));
    }
    @Test
    public void test(){
        UserInfo userInfo=new UserInfo();
        userInfo.setPassword("123");
        userInfo.setName("测试1");
        userInfo.setRealName("ccc");
//        String s = JSONUtil.toJsonStr(userInfo);
        UserInfo userInfo1=new UserInfo();
        userInfo1.setPassword("123");
        userInfo1.setName("测试2");
        userInfo1.setRealName("bbb");
        userInfo1.setNickname("吧吧吧v");
        List<UserInfo> us=new ArrayList<>();
        us.add(userInfo);
        us.add(userInfo1);
        String s = JSONUtil.toJsonStr(us);
        System.out.println(s);

        String a="[{'realName':'ccc','password':'123','name':'测试1'},{'realName':'bbb','password':'123','name':'测试2','nickname':'吧吧吧v'}]";
    }

//    @Test
//    public void test2(){
//        System.out.println(baseReadListener);
//        System.out.println(baseReadListener.getBaseDao());
//        System.out.println(baseReadListener.getReadBatchCount());
//        System.out.println(baseReadListener.getBaseExcelImportValid());
//        System.out.println(1);
//    }
    @Test
    public void test3(){
//        String beanName="excelImportValid";
//        Object daoClassBean = SpringUtil.getBean(beanName);
//        System.out.println(daoClassBean);
        String beanName="userInfoServiceImpl";
        Object daoClassBean = SpringUtil.getBean(beanName);
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
//        Object daoClassBean = wac.getBean(beanName);
        System.out.println(daoClassBean);
    }



}
