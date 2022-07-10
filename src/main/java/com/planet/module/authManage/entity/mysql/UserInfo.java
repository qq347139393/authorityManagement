package com.planet.module.authManage.entity.mysql;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;

import com.planet.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 权限系统-用户（组）-信息表
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserInfo对象", description="用户-信息表")
@TableName("user_info")
public class UserInfo extends BaseEntity<UserInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String name;

    @ApiModelProperty(value = "用户编码")
    private String code;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "账号昵称")
    @ExcelProperty(value="账号昵称",index = 3)
    private String nickname;

    @ApiModelProperty(value = "用户信息")
    private String describ;

    @ApiModelProperty(value = "用户密码")
    private String password;

    //这个值,在[用户修改]接口中用于表示是否修改当前密码的判断:0或空表示不修改;1表示修改
    @ApiModelProperty(value = "加密因子")
    private String salt;

    //在上传文件的接口中,这个字段会用来让前端传递对应MultipartFile文件的文件名,从而让我们在后端可以确定哪个文件对应哪个user
    @ApiModelProperty(value = "头像路径")
    private String portrait;

    @ApiModelProperty(value = "性别")
    private Integer gender;

    @ApiModelProperty(value = "二维码路径")
    private String qrCode;

    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date creatime;

    @TableField(fill = FieldFill.INSERT)
    private String creator;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updator;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleted;

    //前端传过来的参数
    /**
     * 存头像图片名称的字段
     */
    @TableField(exist = false)
    private String originalFilename;
    @TableField(exist = false)//前端传入的验证码
    private String verificationCode;
    @TableField(exist = false)
    private String newPassword;
    @TableField(exist = false)
    private String jwtToken;


}
