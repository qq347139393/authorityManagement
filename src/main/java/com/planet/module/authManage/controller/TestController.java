package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.Test;
import com.planet.module.authManage.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/test")
public class TestController extends BaseControllerImpl<TestService, Test> {

    @RequestMapping(value = "/test1",method = RequestMethod.GET)
    public RspResult test1(){
        System.out.println("成功进入test1方法,此权限开通");
        return RspResult.SUCCESS;
    }

    @RequestMapping(value = "/test2",method = RequestMethod.GET)
    public RspResult test2(){
        System.out.println("成功进入test2方法,此权限开通");
        return RspResult.SUCCESS;
    }

    @RequestMapping(value = "/test3",method = RequestMethod.GET)
    public RspResult test3(){
        System.out.println("成功进入test3方法,此权限开通");
        return RspResult.SUCCESS;
    }
}

