package com.planet.util.jdk8.mapAndEntityConvert.annotation;

import java.lang.annotation.*;

/**
 * 对实体类的属性标识为从Map中读取的指定key对应的value值的注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FieldFromMapKey {
    //当前属性在Map选择key时对应的key值
    String value();
    //提示信息
    String msg() default "";
}
