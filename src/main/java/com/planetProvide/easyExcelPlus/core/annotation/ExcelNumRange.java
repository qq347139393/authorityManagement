package com.planetProvide.easyExcelPlus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段的数值大小必须在指定范围
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelNumRange {
    double min() default 0l;
    double max();
    String message() default "字段的数值大小必须在指定范围";
}
