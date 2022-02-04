package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;

import com.planet.common.base.BaseTreeStructuresEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 权限-权限功能-信息表
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="FunctionInfo对象", description="权限功能-信息表")
@TableName("function_info")
public class FunctionInfo extends BaseTreeStructuresEntity<FunctionInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;

    @ApiModelProperty(value = "权限名称")
    private String name;

    @ApiModelProperty(value = "权限编码")
    private String code;

    @ApiModelProperty(value = "权限信息")
    private String describ;

    @ApiModelProperty(value = "权限后端接口地址")
    private String url;

    @ApiModelProperty(value = "为shiro准备的权限值")
    private String permit;

    @ApiModelProperty(value = "shiro加载的顺序")
    private Integer shiroOrder;

    @ApiModelProperty(value = "权限前端路由地址")
    private String path;

    @ApiModelProperty(value = "前端路由顺序")
    private Integer routeOrder;

//    @ApiModelProperty(value = "关联的父级权限id")
//    private Long parentId;

    @ApiModelProperty(value = "权限等级")
    private Integer level;

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
