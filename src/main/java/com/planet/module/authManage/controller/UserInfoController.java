package com.planet.module.authManage.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * **带文件的请求不支持以/结尾的请求
     * @param multipartFiles
     * @param usersJson
     * @return
     */
    @RequestMapping(value = "/withFile", method = RequestMethod.POST)
//    @Override
    public RspResult inserts(@RequestParam("portraitFiles") MultipartFile[] multipartFiles, @RequestParam("usersJson") String usersJson) {
        if(StrUtil.isEmpty(usersJson)){
            return RspResult.FAILED;
        }
//        return RspResult.SUCCESS;
        JSONArray users = JSONUtil.parseArray(usersJson);
        List<UserInfo> userInfos = JSONUtil.toList(users, UserInfo.class);
        Integer inserts = userInfoService.inserts(multipartFiles,userInfos);
        if(inserts==null){
            return RspResult.FAILED;
        }
        return new RspResult(inserts);
    }

    /**
     * 基础的根据ids修改一条或多条记录
     * @param multipartFiles
     * @param usersJson
     * @return
     */
    @RequestMapping(value ="/withFile",method = RequestMethod.PUT)
//    @Override
    public RspResult updatesByIds(@RequestParam("portraitFiles") MultipartFile[] multipartFiles, @RequestParam("usersJson") String usersJson) {
        if(StrUtil.isEmpty(usersJson)){
            return RspResult.FAILED;
        }
//        return RspResult.SUCCESS;
        JSONArray users = JSONUtil.parseArray(usersJson);
        List<UserInfo> userInfos = JSONUtil.toList(users, UserInfo.class);
        Integer updates = userInfoService.updatesByIds(multipartFiles, userInfos);
        if(updates==null){
            return RspResult.FAILED;
        }

        return new RspResult(updates);
    }

    /**
     * 基础的根据ids删除一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.DELETE)
//    @Override
    public RspResult deletesByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }

        Integer deletes=userInfoService.deletesByIds(ids);
        if(deletes==null){
            return RspResult.FAILED;
        }
        return new RspResult(deletes);
    }

    /**
     * 基础的根据ids查询一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
//    @Override
    public RspResult selectsByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }

        List<UserInfo> userInfos = userInfoService.selectsByIds(ids);
        if(userInfos!=null&&userInfos.size()>0){
            return new RspResult(userInfos);
        }
        return RspResult.FAILED;
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
//    @Override
    public RspResult selectsByPage(@RequestBody UserInfo t) {
        if(t==null){
            return RspResult.FAILED;
        }
        IPage<UserInfo> userInfoIPage = userInfoService.selectsByPage(t);
        if(userInfoIPage==null){
            return RspResult.FAILED;
        }
        return new RspResult(userInfoIPage);
    }

    /**
     * excel导入记录
     * @param excelFile
     * @return
     */
    @RequestMapping(value = "/excelImport",method = RequestMethod.POST)
    public RspResult excelImport(@RequestParam("excelFile") MultipartFile excelFile){
        if(excelFile==null){
            return RspResult.FAILED;
        }
        RspResult rspResult = userInfoService.excelImport(excelFile);
        if(rspResult==null){//发生了异常
            return RspResult.FAILED;
        }
        return rspResult;
    }

    /**
     * excel导出文件
//     * @param t
     * @return
     */
    @RequestMapping(value = "/excelExport",method = RequestMethod.POST)
    public void excelExport(@RequestBody UserInfo t){
        if(t==null){
            return;
        }

        userInfoService.excelExport(t);
    }

}

