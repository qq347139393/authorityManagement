//package com.planet.authoritymanagement;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.planet.module.authManage.ttt.mapper.mysql.TestMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class AuthorityManagementApplicationTests {
//    @Autowired
//    private TestMapper testMapper;
//
//    @Test
//    void contextLoads() {
//    }
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
//
//    @Test
//    public void testPage(){
//        // 测试分页查询
//        //  参数一： 当前页
////  参数二： 页面大小
////  使用了分页插件之后，所有的分页操作也变得简单的！
//        Page<com.planet.module.authManage.entity.mysql.Test> page = new Page<>(2,2);
//        testMapper.selectPage(page,null);
//
//        page.getRecords().forEach(System.out::println);
//        System.out.println(page.getTotal());
//    }
//
//    @Test
//    public void testDelete(){
//        int i = testMapper.deleteById(6l);
//        System.out.println(i);
//    }
//
//}
