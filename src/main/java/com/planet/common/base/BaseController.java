package com.planet.common.base;

import com.planet.common.util.RspResult;

import java.util.List;

/**
 * 基础Controller类的接口
 * @param <T>
 */
public interface BaseController<T> {
    /**
     * 新增一条或多条记录
     * @param list
     * @return
     */
    RspResult inserts(List<T> list);

    /**
     * 根据给定id来查询一条或多条记录
     * @param ids
     * @return
     */
    RspResult selectsByIds(List<Long> ids);

    /**
     * 根据给定id来修改一条或多条记录
     * @param list
     * @return
     */
    RspResult updatesByIds(List<T> list);

    /**
     * 根据给定id来删除一条或多条记录
     * @param ids
     * @return
     */
    RspResult deletesByIds(List<Long> ids);

    /**
     * 根据给定的实体类的属性来查询一条或多条记录
     * @param t
     * @return
     */
    RspResult selects(T t);

    /**
     * 根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    RspResult selectByPage(T t);




}
