package com.planetProvide.easyExcelPlus.core.entity;

import lombok.Data;

import java.util.List;

@Data
public class Msg{
    private String msgName;

    private int msgOrder;

    private List<String> unqualifiedMsg;

}
