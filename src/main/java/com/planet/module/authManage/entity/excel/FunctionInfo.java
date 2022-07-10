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
public class FunctionInfo implements Serializable, BaseRowInterface<FunctionInfo> {

    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

//    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value="权限名称",index = 0)//使用我们的框架,要么都不写index,要么都写index
    @ExcelNotNull
    private String name;

//    @ApiModelProperty(value = "用户编码")
    @ExcelProperty(value="权限编码",index = 1)
    private String code;

//    @ApiModelProperty(value = "真实姓名")
    @ExcelProperty(value="权限信息",index = 2)
    private String describ;

    @ExcelProperty(value="后端接口地址",index = 3)
    private String url;

    @ExcelProperty(value="权限值",index = 4)
    private String permit;

    @ExcelProperty(value="图标值",index = 5)
    private String icon;

    @ExcelProperty(value="路由地址",index = 6)
    private String path;

    @ExcelProperty(value="路由顺序",index = 7)
    private Integer routeOrder;

    @ExcelProperty(value="父级权限id",index = 8)
    private Long parentId;

    @ExcelProperty(value="权限等级",index = 9)
    private Integer type;


    //

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
