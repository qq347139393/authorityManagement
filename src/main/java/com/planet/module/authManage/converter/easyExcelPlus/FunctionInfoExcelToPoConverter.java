package com.planet.module.authManage.converter.easyExcelPlus;

import com.planet.module.authManage.entity.excel.FunctionInfo;
import com.planet.module.authManage.entity.excel.RoleInfo;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FunctionInfoExcelToPoConverter extends BaseExcelToPoConverter<FunctionInfo> {
    @Override
    public List convertExcelToPo(List<FunctionInfo> rows) {
        List<com.planet.module.authManage.entity.mysql.FunctionInfo> functionInfos = rows.stream().map(row -> {
            com.planet.module.authManage.entity.mysql.FunctionInfo functionInfo = new com.planet.module.authManage.entity.mysql.FunctionInfo();
            functionInfo.setName(row.getName());
            functionInfo.setCode(row.getCode());
            functionInfo.setDescrib(row.getDescrib());
            functionInfo.setUrl(row.getUrl());
            functionInfo.setPermit(row.getPermit());
            functionInfo.setIcon(row.getIcon());
            functionInfo.setPath(row.getPath());
            functionInfo.setRouteOrder(row.getRouteOrder());
            functionInfo.setParentId(row.getParentId());
            functionInfo.setType(row.getType());
            return functionInfo;
        }).collect(Collectors.toList());

        return functionInfos;
    }

    @Override
    public List<FunctionInfo> convertPoToExcel(List list) {
        Object functionInfos = list.stream().map(l -> {
            com.planet.module.authManage.entity.mysql.FunctionInfo functionInfo = (com.planet.module.authManage.entity.mysql.FunctionInfo) l;
            FunctionInfo row = new FunctionInfo();
            row.setName(functionInfo.getName());
            row.setCode(functionInfo.getCode());
            row.setDescrib(functionInfo.getDescrib());
            row.setUrl(functionInfo.getUrl());
            row.setPermit(functionInfo.getPermit());
            row.setIcon(functionInfo.getIcon());
            row.setPath(functionInfo.getPath());
            row.setRouteOrder(functionInfo.getRouteOrder());
            row.setParentId(functionInfo.getParentId());
            row.setType(functionInfo.getType());
            return row;
        }).collect(Collectors.toList());

        return (List<FunctionInfo>)functionInfos;
    }
}
