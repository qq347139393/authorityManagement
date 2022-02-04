package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.module.authManage.dao.mapper.RoleLogMapper;
import com.planet.module.authManage.entity.mysql.RoleLog;
import com.planet.module.authManage.service.RoleLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色-历史表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Service
public class RoleLogServiceImpl extends ServiceImpl<RoleLogMapper, RoleLog> implements RoleLogService {

}
