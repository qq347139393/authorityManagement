package com.planet.module.authManage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.module.authManage.dao.mysql.mapper.FunctionLogMapper;
import com.planet.module.authManage.entity.mysql.FunctionLog;
import com.planet.module.authManage.service.FunctionLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限-历史表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Service
public class FunctionLogServiceImpl extends ServiceImpl<FunctionLogMapper, FunctionLog> implements FunctionLogService {

}
