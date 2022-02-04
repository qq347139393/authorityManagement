package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;

import com.planet.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
    private String nickname;

    @ApiModelProperty(value = "用户信息")
    private String describ;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "加密因子")
    private String salt;

    @ApiModelProperty(value = "头像路径")
    private String portrait;

    @ApiModelProperty(value = "二维码路径")
    private String qrCode;

    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date creatime;

    private String creator;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatime;

    private String updator;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleted;


}
