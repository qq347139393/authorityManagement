package com.planet.module.authManage.service.impl;

import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.dao.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.mapper.UserRoleRsMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.entity.redis.UserRoleRs;
import com.planet.module.authManage.service.AccountModuleService;
import com.planet.system.authByShiro.customSettings.CustomUserToken;
import com.planet.system.authByShiro.service.UserShiroService;
import com.planet.system.authByShiro.util.ShiroUtil;
import com.planet.util.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountModuleServiceImpl implements AccountModuleService {
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;
    @Autowired
    private UserShiroService userShiroService;

    @Override
    public RspResult login(String name, String password) {
        String jwtToken = null;
        try {
            //1.进行登录判断,由Shiro的约定方法进行登录判断
            CustomUserToken userToken=new CustomUserToken(name,password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(userToken);
            //2.登录完成之后需要颁发令牌
            String sessionId = ShiroUtil.getShiroSessionId();
            UserInfo userInfo = (UserInfo)ShiroUtil.getPrincipal();
            Long userId=userInfo.getId();
            Map<String,Object> claims = new HashMap<>();
            claims.put("userId",userId);
            claims.put("name",userInfo.getName());
            jwtToken = JwtUtil.createJwt(UtilsConstant.JWT_ISS, UtilsConstant.TTL_JWT_MILLISECOND,
                    sessionId, claims);
            //3.重载当前登录成功的用户的Redis缓存 --更新键值对的时间的技巧:就是查出来后再update原样放回,就能以简单的方式刷新失效时间了
            //1)userId:sessionId的
            userShiroService.userSessionManage(userId,sessionId);
            //2)userId:userInfo的
            String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            Object user = baseMapper.getCache(userInfoKey);
            if (user!=null&&!"".equals(user)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userInfoKey,(UserInfo)user,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                com.planet.module.authManage.entity.mysql.UserInfo mysqlUserInfo = userInfoMapper.selectById(userId);
                UserInfo redisUser=null;
                if(mysqlUserInfo!=null){
                    //b1:将查询的mysql的user对象转成redis的user对象
                    redisUser=new UserInfo();
                    redisUser.setId(mysqlUserInfo.getId());
                    redisUser.setName(mysqlUserInfo.getName());
                    redisUser.setRealName(mysqlUserInfo.getRealName());
                    redisUser.setNickname(mysqlUserInfo.getNickname());
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userInfoKey,redisUser,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
            //3)userId:userRoles的
            //1.先从redis缓存中查询用户信息
            String userRolesKey= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            Object userRoleRs = baseMapper.getCache(userRolesKey);
            if (userRoleRs!=null&&!"".equals(userRoleRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userRolesKey,(UserRoleRs)userRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                List<Long> userIds=new ArrayList<>();
                userIds.add(userId);
                List<com.planet.module.authManage.entity.mysql.UserRoleRs> mysqlUserRoleRs = userRoleRsMapper.selectsByUserIdsGroupUserId(userIds);
                UserRoleRs redisUserRoleRs=null;
                if(mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0 ){
                    //b1:将查询的mysql的UserRoleRs对象转成redis的UserRoleRs对象
                    redisUserRoleRs=new UserRoleRs();
                    List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                    redisUserRoleRs.setRolePermits(rolePermits);
                    redisUserRoleRs.setUserId(userId);
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userRolesKey,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
            //4)userId:userFunctions的
            //1.先从redis缓存中查询用户信息
            String userFunctionsKey= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(userFunctionsKey);
            if (userFunctionRs!=null&&!"".equals(userFunctionRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userFunctionsKey,userFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                List<Long> userIds=new ArrayList<>();
                userIds.add(userId);
                List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(userIds);
                UserFunctionRs redisUserFunctionRs=null;
                if(functionInfos!=null &&functionInfos.size()>0 ){
                    //b1:将查询的mysql的functionInfos对象转成redis的UserFunctionRs对象
                    redisUserFunctionRs=new UserFunctionRs();
                    List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                    redisUserFunctionRs.setFunctionPermits(functionPermits);
                    redisUserFunctionRs.setUserId(userId);
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userFunctionsKey,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
        }catch (Exception e){
            //登陆失败
            e.printStackTrace();
            return RspResult.FAILED;
        }
        //登陆成功,将生成的jwtToken放入响应数据中返回
        return new RspResult(jwtToken);
    }

    @Override
    public UserInfo selectUserByUserId(Long userId) {
        //1.先从redis缓存中查询用户信息
        String key= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
        Object user = baseMapper.getCache(key);
        if (user!=null&&!"".equals(user)){
            return (UserInfo) user;
        }
        //2.缓存中没有,则去数据库中查询用户信息
        com.planet.module.authManage.entity.mysql.UserInfo userInfo = userInfoMapper.selectById(userId);
        UserInfo redisUser=null;
        if(userInfo!=null){
            //1)将查询的mysql的user对象转成redis的user对象
            redisUser=new UserInfo();
            redisUser.setId(userInfo.getId());
            redisUser.setName(userInfo.getName());
            redisUser.setRealName(userInfo.getRealName());
            redisUser.setNickname(userInfo.getNickname());
            //2)将数据库的用户信息更新到缓存中
            baseMapper.creatCache(key,redisUser,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
        }

        //3)返回redis的user对象
        return redisUser;
    }

    @Override
    public UserRoleRs selectRolesByUserId(Long userId) {
        //1.先从redis缓存中查询用户信息
        String key= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
        Object userRoleRs = baseMapper.getCache(key);
        if (userRoleRs!=null&&!"".equals(userRoleRs)){
            return (UserRoleRs) userRoleRs;
        }
        //2.缓存中没有,则去数据库中查询用户信息
        List<Long> userIds=new ArrayList<>();
        userIds.add(userId);
        List<com.planet.module.authManage.entity.mysql.UserRoleRs> mysqlUserRoleRs = userRoleRsMapper.selectsByUserIdsGroupUserId(userIds);
        UserRoleRs redisUserRoleRs=null;
        if(mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0 ){
            //1)将查询的mysql的UserRoleRs对象转成redis的UserRoleRs对象
            redisUserRoleRs=new UserRoleRs();
            List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
            redisUserRoleRs.setRolePermits(rolePermits);
            redisUserRoleRs.setUserId(userId);
            //2)将数据库的用户信息更新到缓存中
            baseMapper.creatCache(key,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
        }

        //3)返回redis的user对象
        return redisUserRoleRs;
    }

    @Override
    public UserFunctionRs selectFunctionsByUserId(Long userId) {
        //1.先从redis缓存中查询用户信息
        String key= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
        Object userFunctionRs = baseMapper.getCache(key);
        if (userFunctionRs!=null&&!"".equals(userFunctionRs)){
            return (UserFunctionRs) userFunctionRs;
        }
        //2.缓存中没有,则去数据库中查询用户信息
        List<Long> userIds=new ArrayList<>();
        userIds.add(userId);
        List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(userIds);
        UserFunctionRs redisUserFunctionRs=null;
        if(functionInfos!=null &&functionInfos.size()>0 ){
            //1)将查询的mysql的functionInfos对象转成redis的UserFunctionRs对象
            redisUserFunctionRs=new UserFunctionRs();
            List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
            redisUserFunctionRs.setFunctionPermits(functionPermits);
            redisUserFunctionRs.setUserId(userId);
            //2)将数据库的用户信息更新到缓存中
            baseMapper.creatCache(key,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
        }

        //3)返回redis的user对象
        return redisUserFunctionRs;
    }
}
