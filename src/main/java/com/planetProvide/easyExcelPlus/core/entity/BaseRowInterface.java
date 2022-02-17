package com.planetProvide.easyExcelPlus.core.entity;

import java.util.List;

/**
 * 让项目的业务实体类实现这个接口,从而避开java单继承的扩展性问题
 * @param <T>
 */
public interface BaseRowInterface<T> {
    long getRowOrder();

    void setRowOrder(long rowOrder);

    int getRowCode();

    void setRowCode(int rowCode);

    List<Msg> getRowMsgs();

    void setRowMsgs(List<Msg> rowMsgs);
}
