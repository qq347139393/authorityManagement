package com.planet.module.authManage.dao.mapper;

import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

/**
 * <p>
 * 权限-用户:角色-关系表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Mapper
public interface UserRoleRsMapper extends BaseMapper<UserRoleRs> {
    /**
     * 根据多个userIds来查询多条角色记录(去重)
     * @param userIds
     * @return
     */
    @Select("<script>" +
            "select ur.user_id,ur.role_id,r.name,r.code,r.describ,r.permit from" +
            " user_role_rs ur INNER JOIN role_info r on ur.role_id=r.id" +
            " where ur.user_id in " +
            " <foreach collection='userIds' item='item' open='(' separator=',' close=')'>" +
            " #{item} "+
            " </foreach>" +
            " GROUP BY ur.role_id" +
            "</script>")
    @Results(id="userRoleMap",value = {
            @Result(id=true,column = "id",property = "id"),//主键
            @Result(column = "role_id",property = "roleId"),//相当于外键
            @Result(property = "roleInfo",column = "role_id",one=@One(select="com.planet.module.authManage.dao.mapper.RoleInfoMapper.selectById",fetchType= FetchType.EAGER))
    })
    List<UserRoleRs> selectsByUserIdsGroupUserId(@Param("userIds") List<Long> userIds);

}
