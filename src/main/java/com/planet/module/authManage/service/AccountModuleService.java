package com.planet.module.authManage.service;

import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.entity.redis.UserRoleRs;

public interface AccountModuleService {

    /**
     * 根据用户名密码
     * @param name
     * @param password
     * @return
     */
    RspResult login(String name,String password);

    /**
     * 根据用户id从redis缓存中获取对应的用户信息
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userId
     * @return
     */
    UserInfo selectUserByUserId(Long userId);

    /**
     * 根据用户id从redis缓存中获取对应的全部角色数据
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userId
     * @return
     */
    UserRoleRs selectRolesByUserId(Long userId);

    /**
     * 根据用户id从redis缓存中获取对应的全部权限数据
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userId
     * @return
     */
    UserFunctionRs selectFunctionsByUserId(Long userId);

}
