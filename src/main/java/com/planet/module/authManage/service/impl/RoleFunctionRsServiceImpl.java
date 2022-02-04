package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.util.TreeStructuresBuildUtilNoField;
import com.planet.module.authManage.dao.mapper.FunctionInfoMapper;
import com.planet.module.authManage.dao.mapper.RoleFunctionRsMapper;
import com.planet.module.authManage.dao.mapper.UserRoleRsMapper;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.RoleFunctionRsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色:权限-关系表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class RoleFunctionRsServiceImpl extends ServiceImpl<RoleFunctionRsMapper, RoleFunctionRs> implements RoleFunctionRsService {
    @Autowired
    private FunctionInfoMapper authFunctionInfoMapper;
    @Autowired
    private RoleFunctionRsMapper roleFunctionRsMapper;
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;

    @Override
    public List<RoleFunctionRs> selectsByRoleId(Long roleId) {
        //1.查全权限
        List<FunctionInfo> authFunctionInfos = authFunctionInfoMapper.selectList(new QueryWrapper<FunctionInfo>());
        //2.根据roleId查询关联的权限
        QueryWrapper<RoleFunctionRs> wrapper=new QueryWrapper<>();
        List<RoleFunctionRs> authRoleFunctionRsList = roleFunctionRsMapper.selectList(new QueryWrapper<RoleFunctionRs>().eq("role_id",roleId));
        //3.进行关联标识的写入
        List<RoleFunctionRs> newAuthRoleFunctionRsList = authFunctionInfos.stream().map(authFunctionInfo -> {
            boolean b = authRoleFunctionRsList.stream().anyMatch(authRoleFunctionRs -> authRoleFunctionRs.getFunctionId().equals(authFunctionInfo.getId()));
            RoleFunctionRs authRoleFunctionRs = new RoleFunctionRs();
            authRoleFunctionRs.setFunctionId(authFunctionInfo.getId());
            authRoleFunctionRs.setOwnId(authFunctionInfo.getId());//此ownId用于进行树状结构处理要用到的字段
            authRoleFunctionRs.setFunctionName(authFunctionInfo.getName());
            authRoleFunctionRs.setFunctionCode(authFunctionInfo.getCode());
            authRoleFunctionRs.setFunctionDesc(authFunctionInfo.getDescrib());
            authRoleFunctionRs.setFunctionPath(authFunctionInfo.getPath());
            authRoleFunctionRs.setFunctionPermit(authFunctionInfo.getPermit());
            authRoleFunctionRs.setFunctionUrl(authFunctionInfo.getUrl());
            authRoleFunctionRs.setParentId(authFunctionInfo.getParentId());
            authRoleFunctionRs.setFunctionLevel(authFunctionInfo.getLevel());
            if (b) authRoleFunctionRs.setRoleId(roleId);
            return authRoleFunctionRs;
        }).collect(Collectors.toList());
        //4.对全权限集合进行树状结构化处理
        return new TreeStructuresBuildUtilNoField<RoleFunctionRs>().buildTree(newAuthRoleFunctionRsList);
    }

    @Transactional
    @Override
    public Integer setRoleAndFunctionRelations(RoleFunctionRs authRoleFunctionRs) {
        Long roleId=authRoleFunctionRs.getRoleId();
        List<Long> functionIds=authRoleFunctionRs.getFunctionIds();
        //1.删除当前角色的旧的权限
        roleFunctionRsMapper.delete(new QueryWrapper<RoleFunctionRs>().eq("role_id",roleId));
        //2.确认当前角色要新添加的权限是否存在,只把存在的权限收集起来进行下一步的关联添加(虽然这一步目前不需要,因为根本不会改变权限..但是以后我们会做成活权限)
        List<FunctionInfo> authFunctionInfos= null;
        if(functionIds!=null&&functionIds.size()>0){
            authFunctionInfos=authFunctionInfoMapper.selectBatchIds(functionIds);
        }
        //3.进行新权限的关联添加
        if(authFunctionInfos!=null&&authFunctionInfos.size()>0){
            List<RoleFunctionRs> authRoleFunctionRsList = authFunctionInfos.stream().map(authFunctionInfo -> {
                RoleFunctionRs authRoleFunctionRs1 = new RoleFunctionRs();
                authRoleFunctionRs1.setRoleId(roleId);
                authRoleFunctionRs1.setFunctionId(authFunctionInfo.getId());
                return authRoleFunctionRs1;
            }).collect(Collectors.toList());
            boolean b = saveBatch(authRoleFunctionRsList);
            //b为false,要进行回滚
            if(!b){
                throw new UnexpectedRollbackException("执行sql异常,要进行事务回滚");
            }
        }
        //4.对用户:权限-记录表进行更新
        //0)查询当前角色下的全部用户,以及每个用户的全部角色
        List<UserRoleRs> authUserRoleRsList = userRoleRsMapper.selectList(new QueryWrapper<UserRoleRs>().eq("role_id", roleId));
        if(authUserRoleRsList==null||authUserRoleRsList.size()==0){
            return 0;//返回值为影响的用户数量
        }

        return authUserRoleRsList.size();//返回值为影响的用户数量
    }
}
