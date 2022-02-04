package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.module.authManage.entity.mysql.Test;
import com.planet.module.authManage.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/test")
public class TestController extends BaseControllerImpl<TestService, Test> {

}

