package com.planet.module.authManage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.ConfigureSys;

import java.util.List;

/**
 * <p>
 * 配置-系统表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
public interface ConfigureSysService extends IService<ConfigureSys> {

    RspResult inserts(List<ConfigureSys> list);

    RspResult updatesByIds(List<ConfigureSys> list);

    RspResult deletesByIds(List<Long> ids);

    RspResult selectsByPage(ConfigureSys t);
}
