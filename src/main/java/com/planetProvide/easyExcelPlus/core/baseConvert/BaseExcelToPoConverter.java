package com.planetProvide.easyExcelPlus.core.baseConvert;

import java.util.List;

public class BaseExcelToPoConverter<T> {
    public List convertExcelToPo(List<T> rows){

        return rows;
    }

    public List<T> convertPoToExcel(List list){
        return list;
    }
}
