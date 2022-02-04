package com.planet.module.authManage.service.impl;

import com.planet.module.authManage.dao.mapper.FunctionInfoMapper;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.service.FunctionInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限功能-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class FunctionInfoServiceImpl extends ServiceImpl<FunctionInfoMapper, FunctionInfo> implements FunctionInfoService {

}
