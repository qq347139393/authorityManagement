package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.RoleLog;
import com.planet.module.authManage.entity.mysql.UserLog;
import com.planet.module.authManage.service.RoleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 角色-历史表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/role-log")
public class RoleLogController /*extends BaseControllerImpl<RoleLogService, RoleLog>*/ {
    @Autowired
    private RoleLogService roleLogService;

    /**
     * 基础的根据ids查询一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
//    @Override
    public RspResult selectsByIds(@RequestParam List<Long> ids) {

        if(ids==null&&ids.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        List<RoleLog> roleLogs = (List<RoleLog>)roleLogService.listByIds(ids);
        return new RspResult(roleLogs);
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
//    @Override
    public RspResult selectsByPage(@RequestBody RoleLog t) {
        if(t==null){
            return RspResult.PAPAMETER_ERROR;
        }
        IPage<RoleLog> userLogIPage = roleLogService.selectsByPage(t);
        if(userLogIPage==null){
            return RspResult.SYS_ERROR;
        }
        return new RspResult(userLogIPage);
    }

    /**
     * excel导出文件
     //     * @param t
     * @return
     */
    @RequestMapping(value = "/excelExport",method = RequestMethod.POST)
    public void excelExport(@RequestBody RoleLog t){
        if(t==null){
            return;
        }

        roleLogService.excelExport(t);
    }

}

