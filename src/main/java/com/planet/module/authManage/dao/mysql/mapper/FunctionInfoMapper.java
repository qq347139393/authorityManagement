package com.planet.module.authManage.dao.mysql.mapper;

import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.planet.module.authManage.entity.mysql.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 权限-权限功能-信息表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Mapper
public interface FunctionInfoMapper extends BaseMapper<FunctionInfo> {

    /**
     * 根据多个functionIds来查询多条用户记录(去重)
     * @param functionIds
     * @return
     */
    @Select("<script>" +
            "select u.id,u.name,u.code,u.real_name,u.nickname,u.describ,u.portrait,u.qr_code,u.status from " +
            " role_function_rs rf inner join role_info r on rf.role_id=r.id " +
            " inner join user_role_rs ur on r.id=ur.role_id " +
            " inner join user_info u on ur.user_id=u.id " +
            " where rf.function_id in " +
            " <foreach collection='functionIds' item='item' open='(' separator=',' close=')'>" +
            " #{item} "+
            " </foreach>" +
            " GROUP BY u.id " +
            "</script>")
    List<UserInfo> selectUsersByFunctionIds(@Param("functionIds") List<Long> functionIds);

}
