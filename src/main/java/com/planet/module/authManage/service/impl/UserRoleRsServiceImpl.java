package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mysql.mapper.RoleFunctionRsMapper;
import com.planet.module.authManage.dao.mysql.mapper.RoleInfoMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserRoleRsMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.service.AccountModuleService;
import com.planet.module.authManage.service.UserRoleRsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户:角色-关系表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
@Slf4j
public class UserRoleRsServiceImpl extends ServiceImpl<UserRoleRsMapper, UserRoleRs> implements UserRoleRsService {
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private AccountModuleService accountModuleService;

    @Override
    public List<UserRoleRs> selectsByUserId(Long userId) {
        //1.查全角色
        List<RoleInfo> authRoleInfos = roleInfoMapper.selectList(new QueryWrapper<RoleInfo>());
        //2.根据userId查询关联的角色
        List<UserRoleRs> authUserRoleRsList = userRoleRsMapper.selectList(new QueryWrapper<UserRoleRs>().eq("user_id", userId));
        //3.进行关联标识的写入
        List<UserRoleRs> newAuthUserRoleRsList = authRoleInfos.stream().map(authRoleInfo -> {
            boolean b = authUserRoleRsList.stream().anyMatch(authUserRoleRs -> authUserRoleRs.getRoleId().equals(authRoleInfo.getId()));
            UserRoleRs authUserRoleRs = new UserRoleRs();
            authUserRoleRs.setRoleId(authRoleInfo.getId());
            authUserRoleRs.setRoleCode(authRoleInfo.getCode());
            authUserRoleRs.setRoleDesc(authRoleInfo.getDescrib());
            authUserRoleRs.setRoleName(authRoleInfo.getName());
            if (b) authUserRoleRs.setUserId(userId);//以此来标识当前用户是否含有这个角色
            return authUserRoleRs;
        }).collect(Collectors.toList());
        return newAuthUserRoleRsList;
    }

    @Transactional
    @Override
    public Integer setUserAndRoleRelations(UserRoleRs authUserRoleRs) {
        Long userId=authUserRoleRs.getUserId();
        List<Long> roleIds=authUserRoleRs.getRoleIds();
        //1.删除当前用户的旧的角色
        userRoleRsMapper.delete(new QueryWrapper<UserRoleRs>().eq("user_id", userId));
        //2.确认当前用户要新添加的角色是否存在,只把存在的角色收集起来进行下一步的关联添加
        List<RoleInfo> authRoleInfos = null;
        if(roleIds!=null&&roleIds.size()>0){
            authRoleInfos = roleInfoMapper.selectBatchIds(roleIds);
        }
        //3.进行新角色的关联添加
        if(authRoleInfos!=null&&authRoleInfos.size()>0){
            List<UserRoleRs> authUserRoleRsList = authRoleInfos.stream().map(authRoleInfo -> {
                UserRoleRs authUserRoleRs1 = new UserRoleRs();
                authUserRoleRs1.setUserId(userId);
                authUserRoleRs1.setRoleId(authRoleInfo.getId());
                return authUserRoleRs1;
            }).collect(Collectors.toList());
            boolean b = saveBatch(authUserRoleRsList);
            //b为false,要进行回滚
            if(!b){
                throw new UnexpectedRollbackException("执行sql异常,要进行事务回滚");
            }
        }
        //4.更新此用户的redis的角色和权限的缓存(如果redis有的话):userInfo,roles,functions
        //0)判断当前redis中是否有此用户的缓存:如果没有的话,就不需要更新了
        String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
        Object user = baseMapper.getCache(userInfoKey);
        if(user==null){//说明此时这个用户还没有在缓存中,所以直接过
            return roleIds.size();
        }
        List<Long> userIds=Arrays.asList(userId);
        //1)更新用户的用户缓存
        boolean b = accountModuleService.updateRedisUserByUserIds(userIds);
        //2)更新用户的角色缓存
        boolean b1 = accountModuleService.updateRedisRolesByUserIds(userIds);
        //3)更新用户的权限缓存
        boolean b2 = accountModuleService.updateRedisFunctionsByUserIds(userIds);
        if(b&&b1&&b2){
            return roleIds.size();
        }
        return null;
    }
}
