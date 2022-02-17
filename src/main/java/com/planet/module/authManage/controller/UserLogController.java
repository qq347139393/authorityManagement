package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserLog;
import com.planet.module.authManage.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户-历史表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/user-log")
public class UserLogController /*extends BaseControllerImpl<UserLogService, UserLog>*/ {
    @Autowired
    private UserLogService userLogService;

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
        List<UserLog> userLogs = (List<UserLog>)userLogService.listByIds(ids);
        return new RspResult(userLogs);
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
//    @Override
    public RspResult selectsByPage(@RequestBody UserLog t) {
        if(t==null){
            return RspResult.FAILED;
        }
        IPage<UserLog> userLogIPage = userLogService.selectsByPage(t);
        if(userLogIPage==null){
            return RspResult.FAILED;
        }
        return new RspResult(userLogIPage);
    }

    /**
     * excel导出文件
     //     * @param t
     * @return
     */
    @RequestMapping(value = "/excelExport",method = RequestMethod.POST)
    public void excelExport(@RequestBody UserLog t){
        if(t==null){
            return;
        }

        userLogService.excelExport(t);
    }

}

