package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.RoleFunctionRsService;
import com.planet.module.authManage.service.RoleInfoService;
import com.planet.module.authManage.service.UserRoleRsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 权限-角色-信息表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/role-info")
public class RoleInfoController extends BaseControllerImpl<RoleInfoService, RoleInfo> {
    @Autowired
    private RoleInfoService roleInfoService;

    /**
     * 基础的新增一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Override
    public RspResult inserts(@RequestBody List<RoleInfo> list) {

        if(list==null&&list.size()<=0){
            return RspResult.FAILED;
        }
        Integer inserts = roleInfoService.inserts(list);
        if(inserts==null){
            return RspResult.FAILED;
        }
        return new RspResult(inserts);
    }

    /**
     * 基础的根据ids修改一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value ="/",method = RequestMethod.PUT)
    @Override
    public RspResult updatesByIds(@RequestBody List<RoleInfo> list) {
        if(list==null&&list.size()<=0){
            return RspResult.FAILED;
        }
        Integer updates = roleInfoService.updatesByIds(list);
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
    @Override
    public RspResult deletesByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }
        Integer deletes=roleInfoService.deletesByIds(ids);

        if(deletes==null){
            return RspResult.FAILED;
        }
        return new RspResult(deletes);
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
        RspResult rspResult = roleInfoService.excelImport(excelFile);
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
    public void excelExport(@RequestBody RoleInfo t){
        if(t==null){
            return;
        }

        roleInfoService.excelExport(t);
    }
}

