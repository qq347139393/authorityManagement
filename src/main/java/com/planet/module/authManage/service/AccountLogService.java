package com.planet.module.authManage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.RoleLog;

/**
 * <p>
 * 个人账号-历史表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-02-06
 */
public interface AccountLogService extends IService<AccountLog> {
    IPage<AccountLog> selectsByPage(AccountLog t);

    void excelExport(AccountLog t);
}
