package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.UserConfigureRs;
import com.planet.module.authManage.service.UserConfigureRsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户-配置-关系表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/user-configure-rs")
public class UserConfigureRsController extends BaseControllerImpl<UserConfigureRsService, UserConfigureRs> {

}

