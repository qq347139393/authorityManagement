package com.planet.module.authManage.service.impl;

import com.planet.module.authManage.dao.mapper.UserInfoMapper;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.system.authByShiro.util.DigestsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Transactional
    @Override
    public Integer inserts(List<UserInfo> list) {
        //所有用户的密码都要进行盐值加密
        list.stream().forEach(user ->{
            Map<String, String> map = DigestsUtil.entryptPassword(user.getPassword());
            user.setPassword(map.get("password"));
            user.setSalt(map.get("salt"));
        });
        //然后进行存入
        boolean b = saveBatch(list);
        if(b){
            return list.size();
        }
        return null;
    }
}
