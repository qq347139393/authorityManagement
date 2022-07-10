package com.planet.module.authManage.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

import cn.hutool.core.clone.Cloneable;

import com.planet.common.base.BaseEntity;
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
public class FunctionInfo extends BaseEntity<FunctionInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    @ApiModelProperty(value = "图标值（对应ui框架的xlink:href的值）")
    private String icon;

    @ApiModelProperty(value = "权限前端路由地址")
    private String path;

    @ApiModelProperty(value = "前端路由顺序")
    private Integer routeOrder;

    @ApiModelProperty(value = "关联的父级权限id")
    private Long parentId;

    @ApiModelProperty(value = "权限等级:菜单/按钮")
    private Integer type;

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

    //构建树状结构用到的参数
    @TableField(exist = false)
    private List<FunctionInfo> children;
    //用于在查询详情接口的响应数据中,给前端返回的除了按钮之外的全部权限的树状结构的List
    @TableField(exist = false)
    private List<FunctionInfo> functionInfos;

    //************前端需要的参数************
//    @TableField(exist = false)
//    private String key;
    //1)用于判断用户是否填值进行选择性匹配的标志
    //2)用于在查询详情接口的响应数据中,给前端标识是否是当前权限的父权限的标志
    @TableField(exist = false)
    private Boolean flag;

}
