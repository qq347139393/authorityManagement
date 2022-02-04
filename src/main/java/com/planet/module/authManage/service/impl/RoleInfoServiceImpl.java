package com.planet.module.authManage.service.impl;

import com.planet.module.authManage.dao.mapper.RoleInfoMapper;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.service.RoleInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class RoleInfoServiceImpl extends ServiceImpl<RoleInfoMapper, RoleInfo> implements RoleInfoService {

}
