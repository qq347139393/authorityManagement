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
public class RoleInfo implements Serializable, BaseRowInterface<RoleInfo> {

    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

//    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value="角色名称",index = 0)//使用我们的框架,要么都不写index,要么都写index
    @ExcelNotNull
    private String name;

//    @ApiModelProperty(value = "用户编码")
    @ExcelProperty(value="角色编码",index = 1)
    private String code;

//    @ApiModelProperty(value = "真实姓名")
    @ExcelProperty(value="角色说明",index = 2)
    private String describ;

//    @ApiModelProperty(value = "账号昵称")
    @ExcelProperty(value="角色标识",index = 3)
    private String permit;

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
