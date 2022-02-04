package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.UserLog;
import com.planet.module.authManage.service.UserLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class UserLogController extends BaseControllerImpl<UserLogService, UserLog> {

}

