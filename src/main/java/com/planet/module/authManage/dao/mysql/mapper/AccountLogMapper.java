package com.planet.module.authManage.dao.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 个人账号-历史表 Mapper 接口
 * </p>
 *
 * @author Planet
 * @since 2022-02-06
 */
@Mapper
public interface AccountLogMapper extends BaseMapper<AccountLog> {

}
