package com.planet.module.authManage.service;

import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.entity.redis.UserRoleRs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountModuleService {

    /**
     * 根据用户名密码
     * @param name
     * @param password
     * @param verificationCode
     * @return
     */
    RspResult login(String name,String password,String verificationCode);


    //=======下面方法,会查询或更新redis缓存========
    /**
     * 根据用户id从redis缓存中获取对应的用户信息
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userIds
     * @return
     */
    List<UserInfo> selectOrUpdateRedisUserByUserIds(List<Long> userIds);

    /**
     * 根据用户id从redis缓存中获取对应的全部角色数据
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userIds
     * @return
     */
    List<UserRoleRs> selectOrUpdateRedisRolesByUserIds(List<Long> userIds);

    /**
     * 根据用户id从redis缓存中获取对应的全部权限数据
     * 如果redis缓存没有,则会去数据库查询并更新到缓存中
     * @param userIds
     * @return
     */
    List<UserFunctionRs> selectOrUpdateRedisFunctionsByUserIds(List<Long> userIds);

    /**
     * 根据用户id更新用户的用户缓存信息
     * @param userIds
     * @return
     */
    boolean updateRedisUserByUserIds(List<Long> userIds);

    /**
     * 根据用户id更新用户的角色列表缓存信息
     * @param userIds
     * @return
     */
    boolean updateRedisRolesByUserIds(List<Long> userIds);

    /**
     * 根据用户id更新用户的权限列表缓存信息
     * @param userIds
     * @return
     */
    boolean updateRedisFunctionsByUserIds(List<Long> userIds);

    /**
     * 获取登录用的验证码
     */
    void getVeriCodeByPic();

    /**
     * 获取用户自己的用户信息
     * @return
     */
    RspResult selectUserByMyId();

    /**
     * 用户修改自己的用户信息
     * @param multipartFile
     * @param usersJson
     * @return
     */
    boolean updateByMyIds(MultipartFile multipartFile, String usersJson);

    /**
     * 修改自己的密码
     * @param t
     * @return
     */
    boolean updateMyPassword(com.planet.module.authManage.entity.mysql.UserInfo t);
}
