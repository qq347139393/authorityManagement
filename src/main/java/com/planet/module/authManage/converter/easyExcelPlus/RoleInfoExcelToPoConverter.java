package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.RoleInfo;
import com.planet.module.authManage.entity.excel.UserInfo;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleInfoExcelToPoConverter extends BaseExcelToPoConverter<RoleInfo> {
    @Override
    public List convertExcelToPo(List<RoleInfo> rows) {
        List<com.planet.module.authManage.entity.mysql.RoleInfo> roleInfos = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.RoleInfo roleInfo = new com.planet.module.authManage.entity.mysql.RoleInfo();
            roleInfo.setName(row.getName());
            roleInfo.setCode(row.getCode());
            roleInfo.setDescrib(row.getDescrib());
            roleInfo.setPermit(row.getPermit());
            return roleInfo;
        }).collect(Collectors.toList());

        return roleInfos;
    }

    @Override
    public List<RoleInfo> convertPoToExcel(List list) {
        Object roleInfos = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.RoleInfo roleInfo = (com.planet.module.authManage.entity.mysql.RoleInfo) l;
            RoleInfo row = new RoleInfo();
            row.setName(roleInfo.getName());
            row.setCode(roleInfo.getCode());
            row.setDescrib(roleInfo.getDescrib());
            row.setPermit(roleInfo.getPermit());
            return row;
        }).collect(Collectors.toList());

        return (List<RoleInfo>)roleInfos;
    }
}
