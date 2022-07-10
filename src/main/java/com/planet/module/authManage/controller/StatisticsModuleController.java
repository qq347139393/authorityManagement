package com.planet.module.authManage.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.service.StatisticsModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 用户账号模块 前端控制器
 *
 * 我们这次的统计采用的都是相对简单的sql查出全部数据,然后在业务层中用流和循环的方式去对比筛选和整合的..
 * 以后我们会尝试直接用一条复杂的sql来直接拿到统计数据
 *
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/statistics-module")
@Slf4j
public class StatisticsModuleController {
    @Autowired
    private StatisticsModuleService statisticsModuleService;

    /**
     * 统计指定时间段(以年\月\日)的指定用户的在线时长,返回数据给前端
     //     * @param usersJson
     */
    @RequestMapping(value="/usersOnlineDurationForData",method = RequestMethod.GET)
    public RspResult usersOnlineDurationForData(@RequestParam("usersJson") String usersJson){
//        JSONObject jsonObject=new JSONObject();
//        jsonObject.set("start","2021-01");
//        jsonObject.set("end","2021-05");
//        jsonObject.set("section",2);
//        Map<Long,String> map=new LinkedHashMap<>();
//        map.put(1l,"用户1");
//        map.put(2l,"用户2");
////        map.put(3l,"用户3");
//        jsonObject.set("users",map);
//        String usersJson=jsonObject.toString();
        if(StrUtil.isEmpty(usersJson)){
            log.error("统计指定时间段(以年\\月\\日)的指定用户的在线时长,返回数据给前端:参数不能为空");
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=statisticsModuleService.usersOnlineDurationForData(usersJson);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 统计指定时间段(以年\月\日)的指定用户的在线时长,以报表文件形式返回
     //     * @param usersJson
     */
    @RequestMapping(value="/usersOnlineDurationForFile",method = RequestMethod.GET)
    public void usersOnlineDurationForFile(@RequestParam("usersJson") String usersJson){
//        JSONObject jsonObject=new JSONObject();
//        jsonObject.set("start","2020-08");
//        jsonObject.set("end","2021-07");
//        jsonObject.set("section",3);
//        Map<Long,String> map=new LinkedHashMap<>();
//        map.put(1l,"用户1");
//        map.put(2l,"用户2");
////        map.put(3l,"用户3");
//        jsonObject.set("users",map);
//        String usersJson=jsonObject.toString();
        if(StrUtil.isEmpty(usersJson)){
            log.error("统计指定时间段(以年\\月\\日)的指定用户的在线时长,并以excel报表文件形式下载失败:参数不能为空");
            return;
        }
        statisticsModuleService.usersOnlineDurationForFile(usersJson);
    }

    /**
     * 统计指定时间段活跃用户,返回数据给前端
     //     * @param usersJson
     */
    @RequestMapping(value="/activeUsersForData",method = RequestMethod.GET)
    public RspResult activeUsersForData(@RequestParam("usersJson") String usersJson){
//        JSONObject jsonObject=new JSONObject();
//        jsonObject.set("start","2021-01");
//        jsonObject.set("end","2021-05");
//        jsonObject.set("section",1);
//        jsonObject.set("threshold",60);
//        String usersJson=jsonObject.toString();
        if(StrUtil.isEmpty(usersJson)){
            log.error("统计指定时间段(以年\\月\\日)活跃用户,返回数据给前端:参数不能为空");
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=statisticsModuleService.activeUsersForData(usersJson);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 统计指定时间段活跃用户,以报表文件形式返回
     //     * @param usersJson
     */
    @RequestMapping(value="/activeUsersForFile",method = RequestMethod.GET)
    public void activeUsersForFile(@RequestParam("usersJson") String usersJson){
//        JSONObject jsonObject=new JSONObject();
//        jsonObject.set("start","2021-01");
//        jsonObject.set("end","2021-05");
//        jsonObject.set("section",1);
//        jsonObject.set("threshold",60);
//        String usersJson=jsonObject.toString();
        if(StrUtil.isEmpty(usersJson)){
            log.error("统计指定时间段(以年\\月\\日)活跃用户,并以excel报表文件形式下载失败:参数不能为空");
            return;
        }
        statisticsModuleService.activeUsersForFile(usersJson);
    }



}
