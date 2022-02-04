package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.module.authManage.dao.mapper.RoleFunctionRsMapper;
import com.planet.module.authManage.dao.mapper.RoleInfoMapper;
import com.planet.module.authManage.dao.mapper.UserRoleRsMapper;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.UserRoleRsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

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
public class UserRoleRsServiceImpl extends ServiceImpl<UserRoleRsMapper, UserRoleRs> implements UserRoleRsService {
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    @Autowired
    private RoleFunctionRsMapper roleFunctionRsMapper;

    @Override
    public List<UserRoleRs> selectsByUserId(Long userId) {
        //1.查全角色
        List<RoleInfo> authRoleInfos = roleInfoMapper.selectList(new QueryWrapper<RoleInfo>());
        //2.根据userId查询关联的角色
        QueryWrapper<UserRoleRs> wrapper=new QueryWrapper<>();
        List<UserRoleRs> authUserRoleRsList = userRoleRsMapper.selectList(new QueryWrapper<UserRoleRs>().eq("user_id", userId));
        //3.进行关联标识的写入
        List<UserRoleRs> newAuthUserRoleRsList = authRoleInfos.stream().map(authRoleInfo -> {
            boolean b = authUserRoleRsList.stream().anyMatch(authUserRoleRs -> authUserRoleRs.getRoleId().equals(authRoleInfo.getId()));
            UserRoleRs authUserRoleRs = new UserRoleRs();
            authUserRoleRs.setRoleId(authRoleInfo.getId());
            authUserRoleRs.setRoleCode(authRoleInfo.getCode());
            authUserRoleRs.setRoleDesc(authRoleInfo.getDescrib());
            authUserRoleRs.setRoleName(authRoleInfo.getName());
            if (b) authUserRoleRs.setUserId(userId);
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


        return roleIds.size();
    }
}
