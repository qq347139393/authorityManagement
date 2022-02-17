package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.RoleLog;
import com.planet.module.authManage.service.AccountLogService;
import com.planet.module.authManage.service.RoleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 个人账号-历史表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-02-06
 */
@RestController
@RequestMapping("/authManage/account-log")
public class AccountLogController /* extends BaseControllerImpl<AccountLogService, AccountLog> */{
    @Autowired
    private AccountLogService accountLogService;

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
        List<AccountLog> accountLogs = (List<AccountLog>)accountLogService.listByIds(ids);
        return new RspResult(accountLogs);
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
//    @Override
    public RspResult selectsByPage(@RequestBody AccountLog t) {
        if(t==null){
            return RspResult.FAILED;
        }
        IPage<AccountLog> accountLogIPage = accountLogService.selectsByPage(t);
        if(accountLogIPage==null){
            return RspResult.FAILED;
        }
        return new RspResult(accountLogIPage);
    }

    /**
     * excel导出文件
     //     * @param t
     * @return
     */
    @RequestMapping(value = "/excelExport",method = RequestMethod.POST)
    public void excelExport(@RequestBody AccountLog t){
        if(t==null){
            return;
        }

        accountLogService.excelExport(t);
    }
}

