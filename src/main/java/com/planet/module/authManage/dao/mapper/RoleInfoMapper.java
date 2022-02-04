package com.planet.module.authManage.dao.mapper;

import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 权限-角色-信息表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Mapper
public interface RoleInfoMapper extends BaseMapper<RoleInfo> {

}
