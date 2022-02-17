package com.planet.system.sysUserOperationLog.annotation;

import com.planet.system.sysUserOperationLog.enumeration.MethodType;
import com.planet.system.sysUserOperationLog.enumeration.ParameterType;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SysUserOperationMethodLog {

//    enum MethodType {INSERT,UPDATE,DELETE};
    MethodType MethodType() ;

//    enum ParameterType {List,Long,BaseEntity};
    ParameterType parameterType() default ParameterType.List;


}
