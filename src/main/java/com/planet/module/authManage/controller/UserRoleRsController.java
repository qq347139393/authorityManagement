package com.planet.module.authManage.controller;


import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.UserRoleRsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 权限-用户:角色-关系表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/user-role-rs")
public class UserRoleRsController /* extends BaseControllerImpl<UserRoleRsService, UserRoleRs> */{
    @Autowired
    private UserRoleRsService userRoleRsService;
    /**
     * 根据用户id查询关联的全部角色
     * @param userId
     * @return
     */
    @RequestMapping(value = "/byUserId",method = RequestMethod.GET)
    public RspResult selectsByUserId(@RequestParam Long userId){
        List<UserRoleRs> authUserRoleRsList = userRoleRsService.selectsByUserId(userId);

        return new RspResult(authUserRoleRsList);
    }

    /**
     * 根据传入的一个用户id和多个角色id来设置该用户的角色(并更新该用户的权限记录)
     * @param authUserRoleRs
     * @return
     */
    @RequestMapping(value = "/setUserAndRoleRelations",method = RequestMethod.POST)
    public RspResult setUserAndRoleRelations(@RequestBody UserRoleRs authUserRoleRs){
        if(authUserRoleRs==null||authUserRoleRs.getUserId()==null){
            return RspResult.FAILED;
        }
        Integer sum = userRoleRsService.setUserAndRoleRelations(authUserRoleRs);
        return sum!=null&&sum>=0?new RspResult(sum):RspResult.FAILED;
    }

}

