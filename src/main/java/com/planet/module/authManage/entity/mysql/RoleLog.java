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
 * 角色-历史表
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="RoleLog对象", description="角色-历史表")
@TableName("role_log")
public class RoleLog extends BaseEntity<RoleLog> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联的角色表id")
    private Long roleId;

    @ApiModelProperty(value = "被操作对象名")
    private String name;

    @ApiModelProperty(value = "操作人id")
    private Long operatorId;

    @ApiModelProperty(value = "操作人名字")
    private String operatorName;

    @ApiModelProperty(value = "操作方式(执行的方法)")
    private String method;

    @ApiModelProperty(value = "操作内容(执行方法的入参)")
    private String content;

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


}
