package com.linhonghong.dilutions.annotations;

import com.linhonghong.dilutions.DilutionsValue;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Linhh on 09/02/2018.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface MethodExtra {
    String value() default DilutionsValue.DILUTIONS_METHOD_EXTRA;
}
