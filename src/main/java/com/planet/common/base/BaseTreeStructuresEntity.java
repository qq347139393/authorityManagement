package com.planet.common.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BaseTreeStructuresEntity<T extends BaseTreeStructuresEntity> extends BaseEntity<T> {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "关联的父级权限id")
    private Long parentId;
    @TableField(exist = false)
    private List<T> children;


}
