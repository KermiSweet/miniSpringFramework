package com.kermi.spring.annotation;


import com.kermi.spring.constants.ScopType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope{
    ScopType value() default ScopType.SINGLETON;
}
