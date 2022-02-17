package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.RoleLog;
import com.planet.module.authManage.entity.excel.UserLog;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleLogExcelToPoConverter extends BaseExcelToPoConverter<RoleLog> {
    @Override
    public List convertExcelToPo(List<RoleLog> rows) {
        List<com.planet.module.authManage.entity.mysql.RoleLog> roleLogs = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.RoleLog roleLog = new com.planet.module.authManage.entity.mysql.RoleLog();
            roleLog.setRoleId(row.getRoleId());
            roleLog.setName(row.getName());
            roleLog.setOperatorId(row.getOperatorId());
            roleLog.setOperatorName(row.getOperatorName());
            roleLog.setMethod(row.getMethod());
            roleLog.setContent(row.getContent());
            return roleLog;
        }).collect(Collectors.toList());

        return roleLogs;
    }

    @Override
    public List<RoleLog> convertPoToExcel(List list) {
        Object roleLogs = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.RoleLog roleLog = (com.planet.module.authManage.entity.mysql.RoleLog) l;
            RoleLog row = new RoleLog();
            row.setRoleId(roleLog.getRoleId());
            row.setName(roleLog.getName());
            row.setOperatorId(roleLog.getOperatorId());
            row.setOperatorName(roleLog.getOperatorName());
            row.setMethod(roleLog.getMethod());
            row.setContent(roleLog.getContent());
            return row;
        }).collect(Collectors.toList());

        return (List<RoleLog>)roleLogs;
    }
}
