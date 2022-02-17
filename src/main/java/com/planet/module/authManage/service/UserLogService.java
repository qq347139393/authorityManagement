package com.planet.module.authManage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserLog;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户-历史表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
public interface UserLogService extends IService<UserLog> {

    IPage<UserLog> selectsByPage(UserLog t);

    void excelExport(UserLog t);
}
