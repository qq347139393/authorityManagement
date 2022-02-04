package com.planet.module.authManage.controller;

import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.AccountModuleService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账号模块 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/account-module")
public class AccountModuleController {
    @Autowired
    private AccountModuleService accountModuleService;

    /**
     * 用户登录接口
     * @param userInfo
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public RspResult login(@RequestBody UserInfo userInfo){
        //1.参数合法性校验
        if(userInfo==null||userInfo.getName()==null||"".equals(userInfo.getName())
                ||userInfo.getPassword()==null||"".equals(userInfo.getPassword())){
            return RspResult.FAILED;
        }

        return accountModuleService.login(userInfo.getName(),userInfo.getPassword());
    }

    /**
     * 用户登出接口
     * 会清空当前用户的redis缓存中的个人信息
     * @return
     */
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public RspResult logout(){
        Subject subject  = SecurityUtils.getSubject();
        if (subject!=null) {
            subject.logout();
        }
        return RspResult.SUCCESS;
    }

}
