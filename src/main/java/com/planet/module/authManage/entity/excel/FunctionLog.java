package com.planet.module.authManage.entity.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.planetProvide.easyExcelPlus.core.annotation.ExcelNotNull;
import com.planetProvide.easyExcelPlus.core.entity.BaseRowInterface;
import com.planetProvide.easyExcelPlus.core.entity.Msg;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FunctionLog implements Serializable, BaseRowInterface<FunctionLog> {

    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

    @ExcelProperty(value="权限id",index = 0)
    @ExcelNotNull
    private Long functionId;
//    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value="权限名",index = 1)//使用我们的框架,要么都不写index,要么都写index
    @ExcelNotNull
    private String name;

    @ExcelProperty(value="操作人id",index = 2)
    private Long operatorId;

    @ExcelProperty(value="操作人名",index = 3)
    private String operatorName;

    @ExcelProperty(value="方法名",index = 4)
    private String method;

    @ExcelProperty(value="方法参数列表",index = 5)
    private String content;



    private long rowOrder;
    @Override
    public long getRowOrder() {
        return rowOrder;
    }

    @Override
    public void setRowOrder(long rowOrder) {
        this.rowOrder=rowOrder;
    }

    private int rowCode;
    @Override
    public int getRowCode() {
        return rowCode;
    }

    @Override
    public void setRowCode(int rowCode) {
        this.rowCode=rowCode;
    }

    private List<Msg> rowMsgs;
    @Override
    public List<Msg> getRowMsgs() {
        return rowMsgs;
    }

    @Override
    public void setRowMsgs(List<Msg> rowMsgs) {
        this.rowMsgs=rowMsgs;
    }
}
