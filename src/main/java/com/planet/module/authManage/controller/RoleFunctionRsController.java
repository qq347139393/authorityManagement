package com.planet.module.authManage.controller;


import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.service.RoleFunctionRsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 权限-角色:权限-关系表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/role-function-rs")
public class RoleFunctionRsController /* extends BaseControllerImpl<AuthRoleFunctionRsService, AuthRoleFunctionRs> */ {
    @Autowired
    private RoleFunctionRsService roleFunctionRsService;

    /**
     * 根据角色id查询关联的全部权限
     * @param roleId
     * @return
     */
    @RequestMapping(value = "/byRoleId",method = RequestMethod.GET)
    public RspResult selectsByRoleId(@RequestParam Long roleId){
        List<RoleFunctionRs> authRoleFunctionRsList = roleFunctionRsService.selectsByRoleId(roleId);

        return new RspResult(authRoleFunctionRsList);
    }

    /**
     * 根据传入的一个角色id和多个权限id来设置该角色的权限(并更新对应用户的权限记录)
     * @param authRoleFunctionRs
     * @return
     */
    @RequestMapping(value = "/setRoleAndFunctionRelations",method = RequestMethod.POST)
    public RspResult setRoleAndFunctionRelations(@RequestBody RoleFunctionRs authRoleFunctionRs){
        if(authRoleFunctionRs==null||authRoleFunctionRs.getRoleId()==null){
            return RspResult.FAILED;
        }
        Integer sum = roleFunctionRsService.setRoleAndFunctionRelations(authRoleFunctionRs);
        return sum>=0?new RspResult(sum):RspResult.FAILED;
    }

}

