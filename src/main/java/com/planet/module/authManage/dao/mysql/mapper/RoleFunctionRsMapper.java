package com.planet.module.authManage.dao.mysql.mapper;

import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

/**
 * <p>
 * 权限-角色:权限-关系表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Mapper
public interface RoleFunctionRsMapper extends BaseMapper<RoleFunctionRs> {
    /**
     * 根据多个roleIds来查询多条权限记录(去重)
     * @param roleIds
     * @return
     */
    @Select("<script>" +
                "select rf.role_id,rf.function_id,f.name,f.code,f.describ,f.url,f.permit,f.path,f.parent_id,f.level,f.status from" +
                " auth_role_function_rs rf inner join auth_function_info f on rf.function_id=f.id" +
                " where rf.role_id in " +
                " <foreach collection='roleIds' item='item' open='(' separator=',' close=')'>" +
                    " #{item} "+
                " </foreach>" +
                " GROUP BY rf.function_id" +
            "</script>")
    @Results(id="roleFunctionMap",value = {
            @Result(id=true,column = "id",property = "id"),//主键
            @Result(column = "function_id",property = "functionId"),//相当于外键
            @Result(property = "authFunctionInfo",column = "function_id",one=@One(select="com.planet.module.authManage.ttt.mapper.AuthFunctionInfoMapper.selectById",fetchType= FetchType.EAGER))
    })
    List<RoleFunctionRs> selectsByRoleIdsGroupFunctionId(@Param("roleIds") List<Long> roleIds);

}
