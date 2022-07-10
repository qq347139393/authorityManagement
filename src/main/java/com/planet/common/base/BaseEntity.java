package com.planet.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BaseEntity<T extends BaseEntity> implements Serializable {
//    private static final long serialVersionUID = 1L;

//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;

//    @TableField(fill = FieldFill.INSERT)
//    private Integer status;

//    @TableField(fill = FieldFill.INSERT)
//    private Date creatime;

//    private String creator;

//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private Date updatime;

//    private String updator;

//    @Version
//    private Long version;

//    @TableLogic
//    private Integer deleted;

    //以下是数据表没有的字段,用于作为与前端交互的参数
    @TableField(exist = false)//模糊查询用到的字段
    private String key;
    /** 总条数 */
    @TableField(exist = false)
    private Long total;
    /** 每页显示条数，默认 10 */
    @TableField(exist = false)
    private Long size;
    /** 当前页，默认 1 */
    @TableField(exist = false)
    private Long current;
    /** 总页数 */
    @TableField(exist = false)
    private Long pages;
    /** 查询数据列表 */
    @TableField(exist = false)
    private List<T> records;


}
