package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

import com.planet.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 权限-用户:角色-关系表
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserRoleRs对象", description="用户:角色-关系表")
@TableName("user_role_rs")
public class UserRoleRs extends BaseEntity<UserRoleRs> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联的用户id")
    private Long userId;

    @ApiModelProperty(value = "关联的角色id")
    private Long roleId;

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
//    @TableLogic
    private Integer deleted;

    //进行sql查询时用到的字段
    @TableField(exist = false)
    private RoleInfo roleInfo;

    @TableField(exist = false)
    private String roleName;
    @TableField(exist = false)
    private String roleCode;
    @TableField(exist = false)
    private String roleDesc;

    //进行前端传入参数用到的字段
    @TableField(exist = false)
    private List<Long> roleIds;

}
