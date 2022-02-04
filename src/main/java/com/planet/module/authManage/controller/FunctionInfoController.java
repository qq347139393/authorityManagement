package com.planet.module.authManage.controller;


import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.common.util.TreeStructuresBuildUtil;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.service.FunctionInfoService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 权限-权限功能-信息表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/function-info")
public class FunctionInfoController extends BaseControllerImpl<FunctionInfoService, FunctionInfo> {

    @RequestMapping(value ="/selectsByTree",method = RequestMethod.GET)
    public RspResult selectsByTree(){
        List<FunctionInfo> list = iService.list();
        if(list==null||list.size()==0){
            return RspResult.FAILED;
        }
        list = new TreeStructuresBuildUtil().buildTree(list);
        return new RspResult(list);
    }

}

