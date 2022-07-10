package com.planet.module.authManage.service;

import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.module.authManage.entity.mysql.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 角色-信息表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
public interface RoleInfoService extends IService<RoleInfo> {

    RspResult inserts(List<RoleInfo> list);

    RspResult updatesByIds(List<RoleInfo> list);

    Integer deletesByIds(List<Long> ids);

    RspResult excelImport(MultipartFile excelFile);

    void excelExport(RoleInfo t);
}
