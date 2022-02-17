package com.planetProvide.easyExcelPlus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段必须为合格的Email格式
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelQualifiedEmail {
    String message() default "字段必须为合格的Email格式";
}
