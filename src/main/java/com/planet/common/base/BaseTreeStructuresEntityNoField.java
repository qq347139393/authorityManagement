package com.planet.common.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BaseTreeStructuresEntityNoField<T extends BaseTreeStructuresEntityNoField> extends BaseEntity<T> {
//    @TableId(value = "id", type = IdType.AUTO)
@TableField(exist = false)
    private Long ownId;
    @TableField(exist = false)
    private Long parentId;
    @TableField(exist = false)
    private List<T> children;


}
