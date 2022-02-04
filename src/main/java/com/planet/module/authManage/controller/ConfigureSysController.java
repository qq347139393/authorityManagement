package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.service.ConfigureSysService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 配置-系统表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/configure-sys")
public class ConfigureSysController extends BaseControllerImpl<ConfigureSysService, ConfigureSys> {

}

