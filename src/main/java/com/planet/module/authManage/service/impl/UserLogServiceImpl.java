package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.module.authManage.dao.mapper.UserLogMapper;
import com.planet.module.authManage.entity.mysql.UserLog;
import com.planet.module.authManage.service.UserLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-历史表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Service
public class UserLogServiceImpl extends ServiceImpl<UserLogMapper, UserLog> implements UserLogService {

}
