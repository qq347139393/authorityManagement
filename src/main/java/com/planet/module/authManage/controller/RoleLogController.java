package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.RoleLog;
import com.planet.module.authManage.service.RoleLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class RoleLogController extends BaseControllerImpl<RoleLogService, RoleLog> {

}

