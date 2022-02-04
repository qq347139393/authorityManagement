package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.FunctionLog;
import com.planet.module.authManage.service.FunctionLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 权限-历史表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/function-log")
public class FunctionLogController extends BaseControllerImpl<FunctionLogService, FunctionLog> {

}

