package com.planet.module.authManage.dao.mapper;

import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * 权限系统-用户（组）-信息表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 根据多个userIds来查询多条权限记录(去重)
     * @param userIds
     * @return
     */
    @Select("<script>" +
            "select f.id,f.name,f.code,f.describ,f.url,f.permit,f.shiro_order,f.path,f.route_order,f.parent_id,f.level,f.status from" +
            " user_role_rs ur INNER JOIN role_info r on ur.role_id=r.id" +
            " INNER JOIN role_function_rs rf on r.id=rf.role_id " +
            " INNER JOIN function_info f on rf.function_id=f.id" +
            " where ur.user_id in " +
            " <foreach collection='userIds' item='item' open='(' separator=',' close=')'>" +
            " #{item} "+
            " </foreach>" +
            " GROUP BY f.id" +
            "</script>")
    List<FunctionInfo> selectFunctionsByUserIds(@Param("userIds") List<Long> userIds);

}
