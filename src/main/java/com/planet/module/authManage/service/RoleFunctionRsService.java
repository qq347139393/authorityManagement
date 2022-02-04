package com.planet.module.authManage.service;

import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色:权限-关系表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
public interface RoleFunctionRsService extends IService<RoleFunctionRs> {
    /**
     * 根据roleId来获取关联记录和全部功能记录
     * @param roleId
     * @return
     */
    List<RoleFunctionRs> selectsByRoleId(Long roleId);

    /**
     * 根据给定的角色id和权限id来重建关联关系
     * @param authRoleFunctionRs
     * @return
     */
    Integer setRoleAndFunctionRelations(RoleFunctionRs authRoleFunctionRs);
}
