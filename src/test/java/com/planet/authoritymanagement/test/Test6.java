package com.planet.authoritymanagement.test;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.planet.module.authManage.service.StatisticsModuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@EnableScheduling // 开启定时任务功能
public class Test6 {
    @Autowired
    StatisticsModuleService statisticsModuleService;
    @Test
    public void test8(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.set("start","2020-10");
        jsonObject.set("end","2021-03");
        Map<Long,String> map=new LinkedHashMap<>();
        map.put(1l,"用户1");
        map.put(2l,"用户2");
//        map.put(3l,"用户3");
        jsonObject.set("users",map);
        statisticsModuleService.usersOnlineDurationForFile(jsonObject.toString());
    }





    public static void main(String[] args) {
//        JSONObject jsonObject=new JSONObject();
//        jsonObject.set("start","2020-10");
//        System.out.println(jsonObject);

//        String jsonStr="{'userPortraitDefaultUrl':'/configureSystem/默认图片测试.jpg'}";
//        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
//        System.out.println(jsonObject);
//        Object start = jsonObject.get("userPortraitDefaultUrl");
//        System.out.println(start);

        List<Integer> list= Arrays.asList(1,2,3);
        List<Object> collect=null;
        try {
            collect = list.stream().map(l -> {
                if(l.equals(3)){
                    throw new RuntimeException("测试");
                }
                return l*2;
            }).collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(collect);


    }




}
