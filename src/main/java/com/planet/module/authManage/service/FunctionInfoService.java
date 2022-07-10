package com.planet.module.authManage.service;

import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.module.authManage.entity.mysql.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 权限功能-信息表 服务类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
public interface FunctionInfoService extends IService<FunctionInfo> {


    RspResult inserts(List<FunctionInfo> list);

    RspResult updatesByIds(List<FunctionInfo> list);

    RspResult deletesByIds(List<Long> ids);

    RspResult selectsByTreeByPage(FunctionInfo t);

    RspResult selectsByIds(List<Long> ids);

    RspResult excelImport(MultipartFile excelFile);

    void excelExport(FunctionInfo t);
}
