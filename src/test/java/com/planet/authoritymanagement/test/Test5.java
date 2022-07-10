package com.planet.authoritymanagement.test;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.service.StatisticsModuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@EnableScheduling // 开启定时任务功能
public class Test5 {
    @Autowired
    private AccountLogMapper accountLogMapper;

    @Test
    public void test1(){
        FunctionInfo f=new FunctionInfo();
//        f.setType(2);
//
//        Integer type=f.getType();
//        type=3;
//
//        System.out.println(f.getType());
//        System.out.println(type);
        f.setCreator("aaa");

        String creator = f.getCreator();
        creator="bbb";
        System.out.println(f.getCreator());
        System.out.println(creator);

    }
    @Test
    public void test2(){
        String a="      ";
        System.out.println(a);
        String trim = a.trim();
        if(trim==null){
            System.out.println("变为null");
        }else if("".equals(trim)){
            System.out.println("变为了空字符串");
        }else{
            System.out.println("变为了其他");
        }

    }
    @Test
    public void test3(){
        List<RoleInfo> roleInfos=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RoleInfo roleInfo=new RoleInfo();
            roleInfo.setId(1l+i);
            roleInfos.add(roleInfo);
        }

        List<RoleInfo> roleInfos1 = roleInfos.stream().map(r -> {
            RoleInfo roleInfo = ObjectUtil.cloneByStream(r);
            Console.log(roleInfo);
            roleInfo.setId(roleInfo.getId() * 2);
            return roleInfo;
        }).collect(Collectors.toList());

        List<RoleInfo> roleInfos2 = roleInfos.stream().map(r -> {
            RoleInfo roleInfo = ObjectUtil.cloneByStream(r);
            Console.log(roleInfo);
            roleInfo.setId(roleInfo.getId() * 3);
            return roleInfo;
        }).collect(Collectors.toList());

        Console.log(roleInfos);
        Console.log(roleInfos1);
        Console.log(roleInfos2);

    }
    @Test
    public void test4(){
        FunctionInfo functionInfo = new FunctionInfo();
        functionInfo.setId(1l);
        FunctionInfo functionInfo1 = ObjectUtil.cloneByStream(functionInfo);
        Console.log(functionInfo1);
    }
    @Test
    public void test5() throws JsonProcessingException {
        FunctionInfo functionInfo = new FunctionInfo();
        functionInfo.setId(1l);
        ObjectMapper objectMapper = new ObjectMapper();
        FunctionInfo functionInfo1 = objectMapper.readValue(objectMapper.writeValueAsString(functionInfo), FunctionInfo.class);
        functionInfo1.setId(3L);

        Console.log(functionInfo);
        Console.log(functionInfo1);

    }

    @Test
    public void test6(){
        String start="2021-01-01";
        String end="2021-05-01";
        List<Long> ids=Arrays.asList(1l,2l);
        QueryWrapper<AccountLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", ids).eq("method", "login");
        int model;
        if (start.length() == 4) {//按年
            model = 0;
            //eg:2022-01-01
            start = start + "-01-01";
            end = end + "-01-01";
        } else if (start.length() == 7) {//按月
            model = 1;
            //eg:2022-06-01
            start = start + "-01";
            end = end + "-01";
        } else {//按日
            model = 2;
            //eg:2022-03-12
        }
//        select * from account_log where user_id=1 and method='login' and
//                (creatime<'2021-01-01' and updatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and creatime<'2021-05-01' and updatime>='2021-05-01') or
//                (creatime<'2021-01-01' and updatime>='2021-05-01')
        queryWrapper.lt("creatime", start).ge("updatime", start).lt("updatime",end).or().
                ge("creatime",start).lt("updatime",end).or().
                ge("creatime",start).lt("creatime",end).ge("updatime",end).or().
                lt("creatime",start).ge("updatime",end);
        //默认按创建时间的升序排列,后面要用来进行配对
        List<AccountLog> accountLogs = accountLogMapper.selectList(queryWrapper.orderByAsc("creatime"));
        for (AccountLog accountLog : accountLogs) {
            System.out.println(accountLog);
        }

    }
    @Test
    public void test7(){
        AccountLog accountLog = accountLogMapper.selectOne(new QueryWrapper<AccountLog>().eq("id",1l).or()
                .eq("deleted",-1));//删除的也要查出来
        System.out.println(accountLog);

    }

    public static void main(String[] args) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.set("start","2021-01-01");
        jsonObject.set("end","2021-05-01");
        Map<Long,String> map=new LinkedHashMap<>();
        map.put(1l,"张三");
        map.put(2l,"李四");
        jsonObject.set("users",map);
        String jsonStr=jsonObject.toString();
        System.out.println(jsonStr);

        JSONObject jsonObject1 = JSONUtil.parseObj(jsonStr);
        String start=jsonObject.getStr("start");
        String end = jsonObject.getStr("end");
        Map users = (Map)jsonObject.get("users");
        System.out.println(users);
//        for (Object aLong : users.keySet()) {
//            System.out.println(users.get(aLong));
//        }
        users.forEach((k,v)->{
            System.out.println(k);
            System.out.println(v);
        });
        List<Long> ids=new ArrayList<>();
        ((Map)users).forEach((key,value)->{
            ids.add(Long.valueOf((String) key));
        });
        Console.log(ids);

        //{"start":"2021-01-01","users":{"1":"张三","2":"李四"},"end":"2021-05-01"}
    }
    @Autowired
    StatisticsModuleService statisticsModuleService;
    @Test
    public void test8(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.set("start","2021-01");
        jsonObject.set("end","2021-05");
        Map<Long,String> map=new LinkedHashMap<>();
        map.put(1l,"用户1");
        map.put(2l,"用户2");
//        map.put(3l,"用户3");
        jsonObject.set("users",map);
//        statisticsModuleService.usersOnlineDuration(jsonObject.toString());
    }



    /**
     * reprot综合练习
     */
    @Test
    public void test06() {

        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/excel/statistical/指定用户在线时长统计模板.xlsx");

        // 目标文件
        String targetFile = "模板写入6-report.xlsx";

        // 写入workbook对象

        ExcelWriter workBook =
                EasyExcel.write(targetFile).withTemplate(templateInputStream).build();

        WriteSheet sheet = EasyExcel.writerSheet().build();

        // 填充配置，开启组合填充换行
        //FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();

        // ****** 准备数据 *******

        HashMap<String, String> dateMap1 = new HashMap<String, String>();
        dateMap1.put("start", "2020-01-01");
        dateMap1.put("end","2020-06-01");
        //之所以分开,是因为要逐行执行,而模板中中间部分时其他地方的数据..所以这里要分开然后按照模板的排序来分别插入
        HashMap<String, String> dateMap2 = new HashMap<String, String>();
        dateMap2.put("nowDate","2022年2月22日");
        dateMap2.put("operatorName","张三");
        dateMap2.put("lineChart","");

        FillData fillData1=new FillData(1l,"用户1",1154l,214l,"2021年2月",412l,"2021年6月",3l);
        FillData fillData2=new FillData(2l,"用户2",1421l,265l,"2021年3月",535l,"2021年5月",24l);
        FillData fillData3=new FillData(2l,"用户3",154l,34l,"2021年2月",48l,"2021年6月",0l);
        List<FillData> fillDatas=Arrays.asList(fillData1,fillData2,fillData3);


        // 写入统计数据
        workBook.fill(dateMap1, sheet);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();
        // 填充并换行
        workBook.fill(fillDatas, fillConfig, sheet);
        workBook.fill(dateMap2, sheet);

        workBook.finish();

    }

}
