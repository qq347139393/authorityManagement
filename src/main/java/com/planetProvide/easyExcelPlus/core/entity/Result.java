package com.planetProvide.easyExcelPlus.core.entity;

import lombok.Data;

import java.util.List;
@Data
public class Result<T>{
    private int resultCode;
    private List<T> unqualifiedRows;
    private long start;
    private long end;
}
