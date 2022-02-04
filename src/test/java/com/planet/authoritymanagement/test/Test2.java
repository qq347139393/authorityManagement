//package com.planet.authoritymanagement.test;
//
//import com.planet.module.authManage.entity.mysql.FunctionInfo;
//import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
//import com.planet.module.authManage.entity.mysql.UserInfo;
//import com.planet.module.authManage.dao.mapper.mysql.FunctionInfoMapper;
//import com.planet.module.authManage.dao.mapper.mysql.RoleFunctionRsMapper;
//import com.planet.module.authManage.dao.mapper.mysql.UserInfoMapper;
//import com.planet.module.authManage.dao.mapper.mysql.TestMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@SpringBootTest
//public class Test2 {
//    @Autowired
//    private TestMapper testMapper;
//    @Autowired
//    private RoleFunctionRsMapper roleFunctionRsMapper;
//    @Autowired
//    private FunctionInfoMapper functionInfoMapper;
//    @Autowired
//    private UserInfoMapper userInfoMapper;
//
//    @Test
//    public void testInsert(){
//        com.planet.module.authManage.entity.mysql.Test test=new com.planet.module.authManage.entity.mysql.Test();
//        test.setAge(12);
//        test.setCreator("ttt");
//        test.setUpdator("rrr");
//        test.setName("ffs");
//
//        int insert = testMapper.insert(test);
//        System.out.println(insert);
//
//    }
//    @Test
//    public void testSelectUser(){
//        UserInfo authUserInfo = userInfoMapper.selectById(1l);
//        System.out.println(authUserInfo);
//    }
//    @Test
//    public void testSelectFunction(){
//        FunctionInfo authFunctionInfo = functionInfoMapper.selectById(1l);
//        System.out.println(authFunctionInfo);
//    }
//
//    @Test
//    public void testSelectAnno(){
//        List<Long> list=new ArrayList<>();
//        list.add(1l);
//        list.add(2l);
//        List<RoleFunctionRs> authRoleFunctionRs = roleFunctionRsMapper.selectsByRoleIdsGroupFunctionId(list);
//        for (RoleFunctionRs authRoleFunctionR : authRoleFunctionRs) {
//            System.out.println(authRoleFunctionR);
//        }
//
//    }
//
//
//
//
//
//}
