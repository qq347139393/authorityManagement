package com.planet.module.authManage.dao.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.planet.module.authManage.entity.mysql.FunctionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 权限-历史表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Mapper
public interface FunctionLogMapper extends BaseMapper<FunctionLog> {

}
