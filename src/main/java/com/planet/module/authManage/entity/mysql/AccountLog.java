package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import com.planet.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 个人账号-历史表
 * </p>
 *
 * @author Planet
 * @since 2022-02-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AccountLog对象", description="个人账号-历史表")
@TableName("account_log")
public class AccountLog extends BaseEntity<AccountLog> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联用户id")
    private Long userId;

    @ApiModelProperty(value = "用户账号名")
    private String name;

    @ApiModelProperty(value = "操作方式(执行的方法)")
    private String method;

    @ApiModelProperty(value = "操作内容(执行方法的入参)")
    private String content;

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
