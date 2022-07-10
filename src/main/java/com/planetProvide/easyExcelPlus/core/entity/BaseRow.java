package com.planetProvide.easyExcelPlus.core.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Deprecated
public class BaseRow<T> implements Serializable {
    /** 当前记录的编号 */
    private long rowOrder;

    /** 成功或失败的标识值 */
    private int rowCode;
    /** 失败的原因集合 */
    private List<Msg> rowMsgs;


}


