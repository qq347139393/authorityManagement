package com.planet.module.authManage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
     * @param multipartFiles
     * @param list
     * @return
     */
    Integer inserts(MultipartFile[] multipartFiles, List<UserInfo> list);

    /**
     * 修改一条或多条用户记录
     * @param multipartFiles
     * @param list
     * @return
     */
    Integer updatesByIds(MultipartFile[] multipartFiles, List<UserInfo> list);

    /**
     * 根据多个id删除多条用户记录
     * @param ids
     * @return
     */
    Integer deletesByIds(List<Long> ids);

    /**
     * 根据一个或多个id查询一个或多个用户信息
     * @param ids
     * @return
     */
    List<UserInfo> selectsByIds(List<Long> ids);

    /**
     * 分页查询多条用户记录
     * @param t
     * @return
     */
    IPage<UserInfo> selectsByPage(UserInfo t);

    /**
     * excel导入记录
     * @param excelFile
     * @return
     */
    RspResult excelImport(MultipartFile excelFile);

    /**
     * excel导出文件
     * @param t
     */
    void excelExport(UserInfo t);
}
