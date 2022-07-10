package com.planet.authoritymanagement.test;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.module.authManage.dao.mysql.mapper.RoleInfoMapper;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.service.StatisticsModuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@EnableScheduling // 开启定时任务功能
public class Test7 {
    @Autowired
    StatisticsModuleService statisticsModuleService;
    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Test
    public void test1() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        List<Class> parameterClasses=new ArrayList<>();
        Class parameterClass= Wrapper.class;
        parameterClasses.add(parameterClass);

        String methodName="selectList";

        List<Object> parameterValues=new ArrayList<>();
//        RoleInfo parameterValue=new RoleInfo();
//        parameterValue.setName("测试角色6");
//        parameterValue.setCode("00006");
        QueryWrapper<RoleInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("name","测试角色3");
        parameterValues.add(queryWrapper);

        ////////////////
        Class cls=roleInfoMapper.getClass();
        //1.获取指定的方法
        Object[] parameterClassObjs=parameterClasses.toArray();
        Class[] parameterClassArray=new Class[parameterClassObjs.length];
        for (int i=0;i<parameterClassObjs.length;i++){
            parameterClassArray[i]=(Class)parameterClassObjs[i];
        }
        Method[] methods = cls.getMethods();
        Method[] declaredMethods = cls.getDeclaredMethods();
        Method method=cls.getMethod(methodName,parameterClassArray);
        //2.执行指定方法,然后返回结果
        method.setAccessible(true);
        Object result = method.invoke(roleInfoMapper, parameterValues.toArray());
        System.out.println(result);
    }

    public static void main(String[] args) throws NoSuchMethodException {

    }




}
