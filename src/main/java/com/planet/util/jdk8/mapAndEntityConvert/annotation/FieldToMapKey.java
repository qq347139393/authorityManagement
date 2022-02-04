package com.planet.util.jdk8.mapAndEntityConvert.annotation;

import java.lang.annotation.*;

/**
 * 对实体类的属性标识为向Map中构建指定key值的注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FieldToMapKey {
    //当前属性转成Map的key时对应的key值
    String value();
    //提示信息
    String msg() default "";
}
