package com.planet.module.authManage.entity.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UsersOnlineDurationFill {
    @ExcelIgnore
    private Long id;

    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private long total;

    @ExcelProperty(index = 2)
    private long average;

    @ExcelProperty(index = 3)
    private String maxDate;

    @ExcelProperty(index = 4)
    private long max;

    @ExcelProperty(index = 5)
    private String minDate;

    @ExcelProperty(index = 6)
    private long min;

}
