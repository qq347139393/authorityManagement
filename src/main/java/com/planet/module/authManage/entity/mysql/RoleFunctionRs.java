package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

import com.planet.common.base.BaseTreeStructuresEntityNoField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 权限-角色:权限-关系表
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="RoleFunctionRs对象", description="角色:权限-关系表")
@TableName("role_function_rs")
public class RoleFunctionRs extends BaseTreeStructuresEntityNoField<RoleFunctionRs> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联的角色id")
    private Long roleId;

    @ApiModelProperty(value = "关联的权限id")
    private Long functionId;

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
//    @TableLogic
    private Integer deleted;

    //构建sql用的字段
    @TableField(exist = false)
    private FunctionInfo authFunctionInfo;

    @TableField(exist = false)
    private String functionName;
    @TableField(exist = false)
    private String functionCode;
    @TableField(exist = false)
    private String functionDesc;
    @TableField(exist = false)
    private String functionUrl;
    @TableField(exist = false)
    private String functionPermit;
    @TableField(exist = false)
    private String functionPath;
//    @TableField(exist = false)
//    private Long functionParentId;
    @TableField(exist = false)
    private Integer functionLevel;

    //进行前端传入参数用到的字段
    @TableField(exist = false)
    private List<Long> functionIds;
}
