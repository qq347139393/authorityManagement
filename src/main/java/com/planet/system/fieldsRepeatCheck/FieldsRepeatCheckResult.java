package com.planet.system.fieldsRepeatCheck;

import lombok.Data;

import java.util.Map;

/**
 * 存字段重复性校验结果的类
 * @param <T>
 */
@Data
public class FieldsRepeatCheckResult<T> {

    private Boolean result;

    private Map<String,Object> errorMap;

    private T data;

}
