package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.UserInfoService;
import com.planet.module.authManage.service.UserRoleRsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 权限系统-用户（组）-信息表 前端控制器
 * </p>
 * 涉及到密码和关联,所以这个user模块不能用模板方法而是要单独做
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/user-info")
public class UserInfoController /*extends BaseControllerImpl<UserInfoService, UserInfo>*/ {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 基础的新增一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
//    @Override
    public RspResult inserts(@RequestBody List<UserInfo> list) {

        if(list==null&&list.size()==0){
            return RspResult.FAILED;
        }

        Integer inserts = userInfoService.inserts(list);
        if(inserts==null){
            return RspResult.FAILED;
        }
        return new RspResult(inserts);
    }


}

