package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.AccountLog;
import com.planet.module.authManage.entity.excel.RoleLog;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountLogExcelToPoConverter extends BaseExcelToPoConverter<AccountLog> {
    @Override
    public List convertExcelToPo(List<AccountLog> rows) {
        List<com.planet.module.authManage.entity.mysql.AccountLog> accountLogs = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.AccountLog accountLog = new com.planet.module.authManage.entity.mysql.AccountLog();
            accountLog.setUserId(row.getUserId());
            accountLog.setName(row.getName());
//            accountLog.setOperatorId(row.getOperatorId());
//            accountLog.setOperatorName(row.getOperatorName());
            accountLog.setMethod(row.getMethod());
            accountLog.setContent(row.getContent());
            return accountLog;
        }).collect(Collectors.toList());

        return accountLogs;
    }

    @Override
    public List<AccountLog> convertPoToExcel(List list) {
        Object accountLogs = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.AccountLog accountLog = (com.planet.module.authManage.entity.mysql.AccountLog) l;
            AccountLog row = new AccountLog();
            row.setUserId(accountLog.getUserId());
            row.setName(accountLog.getName());
//            row.setOperatorId(accountLog.getOperatorId());
//            row.setOperatorName(accountLog.getOperatorName());
            row.setMethod(accountLog.getMethod());
            row.setContent(accountLog.getContent());
            return row;
        }).collect(Collectors.toList());

        return (List<AccountLog>)accountLogs;
    }
}
