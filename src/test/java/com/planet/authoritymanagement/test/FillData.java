package com.planet.authoritymanagement.test;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FillData {
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

    public FillData() {
    }

    public FillData(Long id, String name, long total, long average, String maxDate, long max, String minDate, long min) {
        this.id = id;
        this.name = name;
        this.total = total;
        this.average = average;
        this.maxDate = maxDate;
        this.max = max;
        this.minDate = minDate;
        this.min = min;
    }
}
