package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.RoleFunctionRsService;
import com.planet.module.authManage.service.RoleInfoService;
import com.planet.module.authManage.service.UserRoleRsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 权限-角色-信息表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/role-info")
public class RoleInfoController extends BaseControllerImpl<RoleInfoService, RoleInfo> {
    @Autowired
    private UserRoleRsService userRoleRsService;
    @Autowired
    private RoleFunctionRsService roleFunctionRsService;

    /**
     * 基础的根据ids删除一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/{ids}",method = RequestMethod.DELETE)
    @Override
    public RspResult deletesByIds(@PathVariable List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }else if(ids.size()==1){
            //判断是否有关联的记录
            //根据roleId查询关联的全部用户:如果有,则禁止删除;如果没有,则执行删除
            List<UserRoleRs> authUserRoleRsList = userRoleRsService.list(new QueryWrapper<UserRoleRs>().eq("role_id", ids.get(0)));
            //根据roleId查询关联的全部权限:如果有,则禁止删除;如果没有,则执行删除
            List<RoleFunctionRs> AuthRoleFunctionRsList = roleFunctionRsService.list(new QueryWrapper<RoleFunctionRs>().eq("role_id", ids.get(0)));
            if((authUserRoleRsList==null||authUserRoleRsList.size()==0)&&(AuthRoleFunctionRsList==null||AuthRoleFunctionRsList.size()==0)){//当前角色已经没有了关联的用户或关联的权限,允许删除
                boolean b = iService.removeById((Long) ids.get(0));
                return b==true?RspResult.SUCCESS:RspResult.FAILED;
            }else{//当前角色已经有了关联的用户或关联的权限,禁止删除
                return RspResult.FAILED;
            }

        }else{
            //判断是否有关联的记录
            List<UserRoleRs> authUserRoleRsList = userRoleRsService.list(new QueryWrapper<UserRoleRs>().in("role_id", ids));
            List<RoleFunctionRs> AuthRoleFunctionRsList = roleFunctionRsService.list(new QueryWrapper<RoleFunctionRs>().eq("role_id", ids));
            if((authUserRoleRsList==null||authUserRoleRsList.size()==0)&&(AuthRoleFunctionRsList==null||AuthRoleFunctionRsList.size()==0)){//当前所有的用户已经没有了关联的角色,允许删除
                boolean b = iService.removeByIds(ids);
                return b==true?RspResult.SUCCESS:RspResult.FAILED;
            }else{//当前某个角色已经有了关联的用户或关联的权限,禁止删除
                return RspResult.FAILED;
            }
        }
    }
}

