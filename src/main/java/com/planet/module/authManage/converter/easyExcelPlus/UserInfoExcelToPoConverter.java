package com.planet.module.authManage.converter.easyExcelPlus;

import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import com.planet.module.authManage.entity.excel.UserInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserInfoExcelToPoConverter extends BaseExcelToPoConverter<UserInfo> {
    @Override
    public List convertExcelToPo(List<UserInfo> rows) {
        List<com.planet.module.authManage.entity.mysql.UserInfo> userInfos = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.UserInfo userInfo = new com.planet.module.authManage.entity.mysql.UserInfo();
            userInfo.setName(row.getName());
            userInfo.setCode(row.getCode());
            userInfo.setRealName(row.getRealName());
            userInfo.setNickname(row.getNickname());
            userInfo.setDescrib(row.getDescrib());
            userInfo.setPassword(row.getPassword());
            userInfo.setPortrait(row.getPortrait());
            return userInfo;
        }).collect(Collectors.toList());

        return userInfos;
    }

    @Override
    public List<UserInfo> convertPoToExcel(List list) {
        Object userInfos = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.UserInfo userInfo = (com.planet.module.authManage.entity.mysql.UserInfo) l;
            UserInfo row = new UserInfo();
            row.setName(userInfo.getName());
            row.setCode(userInfo.getCode());
            row.setRealName(userInfo.getRealName());
            row.setNickname(userInfo.getNickname());
            row.setDescrib(userInfo.getDescrib());
            row.setPassword("保密");
            row.setPortrait(userInfo.getPortrait());
            return row;
        }).collect(Collectors.toList());

        return (List<UserInfo>)userInfos;
    }
}
