package com.planet.module.authManage.service;

import com.planet.module.authManage.entity.mysql.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户-信息表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
public interface UserInfoService extends IService<UserInfo> {
    /**
     * 新增一条或多条用户记录
     * @param list
     * @return
     */
    Integer inserts(List<UserInfo> list);

}
