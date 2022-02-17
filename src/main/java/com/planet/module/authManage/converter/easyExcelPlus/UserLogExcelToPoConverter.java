package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.UserLog;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserLogExcelToPoConverter extends BaseExcelToPoConverter<UserLog> {
    @Override
    public List convertExcelToPo(List<UserLog> rows) {
        List<com.planet.module.authManage.entity.mysql.UserLog> userLogs = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.UserLog userLog = new com.planet.module.authManage.entity.mysql.UserLog();
            userLog.setUserId(row.getUserId());
            userLog.setName(row.getName());
            userLog.setOperatorId(row.getOperatorId());
            userLog.setOperatorName(row.getOperatorName());
            userLog.setMethod(row.getMethod());
            userLog.setContent(row.getContent());
            return userLog;
        }).collect(Collectors.toList());

        return userLogs;
    }

    @Override
    public List<UserLog> convertPoToExcel(List list) {
        Object userLogs = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.UserLog userLog = (com.planet.module.authManage.entity.mysql.UserLog) l;
            UserLog row = new UserLog();
            row.setUserId(userLog.getUserId());
            row.setName(userLog.getName());
            row.setOperatorId(userLog.getOperatorId());
            row.setOperatorName(userLog.getOperatorName());
            row.setMethod(userLog.getMethod());
            row.setContent(userLog.getContent());
            return row;
        }).collect(Collectors.toList());

        return (List<UserLog>)userLogs;
    }
}
