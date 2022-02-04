package com.planet.module.authManage.service;

import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户:角色-关系表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
public interface UserRoleRsService extends IService<UserRoleRs> {
    /**
     * 根据userId来获取关联记录和全部角色记录
     * @param userId
     * @return
     */
    List<UserRoleRs> selectsByUserId(Long userId);

    /**
     * 根据给定的用户id和角色id来重建关联关系
     * @param authUserRoleRs
     * @return
     */
    Integer setUserAndRoleRelations(UserRoleRs authUserRoleRs);

}
