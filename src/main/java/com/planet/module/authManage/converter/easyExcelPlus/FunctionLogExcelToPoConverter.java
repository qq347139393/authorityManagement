package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.FunctionLog;
import com.planet.module.authManage.entity.excel.RoleLog;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FunctionLogExcelToPoConverter extends BaseExcelToPoConverter<FunctionLog> {
    @Override
    public List convertExcelToPo(List<FunctionLog> rows) {
        List<com.planet.module.authManage.entity.mysql.FunctionLog> functionLogs = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.FunctionLog functionLog = new com.planet.module.authManage.entity.mysql.FunctionLog();
            functionLog.setFunctionId(row.getFunctionId());
            functionLog.setName(row.getName());
            functionLog.setOperatorId(row.getOperatorId());
            functionLog.setOperatorName(row.getOperatorName());
            functionLog.setMethod(row.getMethod());
            functionLog.setContent(row.getContent());
            return functionLog;
        }).collect(Collectors.toList());

        return functionLogs;
    }

    @Override
    public List<FunctionLog> convertPoToExcel(List list) {
        Object functionLogs = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.FunctionLog functionLog = (com.planet.module.authManage.entity.mysql.FunctionLog) l;
            FunctionLog row = new FunctionLog();
            row.setFunctionId(functionLog.getFunctionId());
            row.setName(functionLog.getName());
            row.setOperatorId(functionLog.getOperatorId());
            row.setOperatorName(functionLog.getOperatorName());
            row.setMethod(functionLog.getMethod());
            row.setContent(functionLog.getContent());
            return row;
        }).collect(Collectors.toList());

        return (List<FunctionLog>)functionLogs;
    }
}
