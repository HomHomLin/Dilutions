package com.linhonghong.dilutions.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Linhh on 16/11/29.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ProtocolPath {
    String value() default "";
}
