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
 * 用户-配置-关系表
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserConfigureRs对象", description="用户:配置-关系表")
@TableName("user_configure_rs")
public class UserConfigureRs extends BaseEntity<UserConfigureRs> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联的用户id")
    private Long userId;

    @ApiModelProperty(value = "关联的配置系统表id")
    private Long configureId;

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


}
