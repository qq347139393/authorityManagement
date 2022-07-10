package com.planet.module.authManage.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.AccountModuleService;
import com.planet.util.springBoot.WebUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
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
            return RspResult.PAPAMETER_ERROR;
        }

        return accountModuleService.login(userInfo.getName(),userInfo.getPassword(),userInfo.getVerificationCode());
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

    /**
     * 是否需要获取验证码的接口
     * 前端页面根据返回值再决定是否发送获取图片验证码的请求,还是直接渲染没有验证码的登录页
     * @return
     */
    @RequestMapping(value = "/isVeriCodeByPic",method = RequestMethod.GET)
    public RspResult isGetVeriCodeByPic(){
        RspResult rspResult=accountModuleService.isGetVeriCodeByPic();
        if(rspResult==null){
            return RspResult.FAILED;
        }
        return rspResult;
    }

    /**
     * 获取图片验证码
     * 在登录界面展示给用户之前调用此方法获取图片,该数据要配合sessionId存入redis缓存中
     * @return
     */
    @RequestMapping(value = "/veriCodeByPic",method = RequestMethod.GET)
    public RspResult getVeriCodeByPic(){
        return accountModuleService.getVeriCodeByPic();
    }

    @RequestMapping(value = "/checkToken",method = RequestMethod.POST)
    public RspResult checkToken(@RequestBody UserInfo user){
        //需要name和jwtToken
        if(user==null||StrUtil.isEmpty(user.getJwtToken())||StrUtil.isEmpty(user.getName())){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=accountModuleService.checkToken(user);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 获取用户自己的用户信息
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public RspResult selectUserByMyId(){

        return accountModuleService.selectUserByMyId();
    }

    /**
     * 用户修改自己的用户信息
     * 不能修改密码
     * @param multipartFile
     * @param usersJson
     * @return
     */
    @RequestMapping(value ="/withFile",method = RequestMethod.PUT)
//    @Override
    public RspResult updateByMyIds(@RequestParam("portraitFile") MultipartFile multipartFile, @RequestParam("usersJson") String usersJson) {
        if(StrUtil.isEmpty(usersJson)){
            return RspResult.FAILED;
        }
        RspResult rspResult=accountModuleService.updateByMyIds(multipartFile,usersJson);
        if(rspResult==null){
            return RspResult.SYS_ERROR;
        }
        return rspResult;
    }

//    /**
//     * 获取用户自己的系统定制化配置
//     * @return
//     */
//    @RequestMapping(value = "/system",method = RequestMethod.GET)
//    public RspResult selectSystemByMyId(){
//
//        return accountModuleService.selectSystemByMyId();
//    }

//    /**
//     * 用户修改自己的用户信息
//     * 不能修改密码
//     * @param multipartFile
//     * @param usersJson
//     * @return
//     */
//    @RequestMapping(value ="/system",method = RequestMethod.PUT)
////    @Override
//    public RspResult updateSystemByMyId(@RequestParam("portraitFile") MultipartFile multipartFile, @RequestParam("usersJson") String usersJson) {
//        if(StrUtil.isEmpty(usersJson)){
//            return RspResult.FAILED;
//        }
//        boolean b=accountModuleService.updateSystemByMyId(multipartFile,usersJson);
//        if(b){
//            return RspResult.SUCCESS;
//        }
//        return RspResult.FAILED;
//    }

    /**
     * 修改自己的密码
     * @param t
     * @return
     */
    @RequestMapping(value ="/password",method = RequestMethod.PUT)
    public RspResult updateMyPassword(@RequestBody UserInfo t){
        if(t==null||StrUtil.isEmpty(t.getPassword())||StrUtil.isEmpty(t.getNewPassword())){
            return RspResult.PAPAMETER_ERROR;
        }

        boolean b=accountModuleService.updateMyPassword(t);
        if(b){
            return RspResult.SUCCESS;
        }
        return RspResult.FAILED;
    }

}
