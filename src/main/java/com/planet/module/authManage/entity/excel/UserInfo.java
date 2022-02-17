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
public class UserInfo implements Serializable, BaseRowInterface<UserInfo> {

    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

//    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value="用户名",index = 0)//使用我们的框架,要么都不写index,要么都写index
    @ExcelNotNull
    private String name;

//    @ApiModelProperty(value = "用户编码")
    @ExcelProperty(value="用户编码",index = 1)
    private String code;

//    @ApiModelProperty(value = "真实姓名")
    @ExcelProperty(value="真实姓名",index = 2)
    private String realName;

//    @ApiModelProperty(value = "账号昵称")
    @ExcelProperty(value="账号昵称",index = 3)
    private String nickname;

//    @ApiModelProperty(value = "用户信息")
    @ExcelProperty(value="用户信息",index = 4)
    private String describ;

//    @ApiModelProperty(value = "用户密码")
    @ExcelProperty(value="用户密码",index = 5)
    private String password;

    //在上传文件的接口中,这个字段会用来让前端传递对应MultipartFile文件的文件名,从而让我们在后端可以确定哪个文件对应哪个user
//    @ApiModelProperty(value = "头像路径")
    @ExcelProperty(value="头像路径",index = 6)
    private String portrait;

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
