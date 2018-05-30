package com.linhonghong.dilutions.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Linhh on 17/6/22.
 */

@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Param {
    String value() default "";
}
