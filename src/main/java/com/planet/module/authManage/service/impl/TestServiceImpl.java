package com.planet.module.authManage.service.impl;

import com.planet.module.authManage.dao.mysql.mapper.TestMapper;
import com.planet.module.authManage.entity.mysql.Test;
import com.planet.module.authManage.service.TestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test> implements TestService {

}
