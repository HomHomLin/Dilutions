package com.linhonghong.dilutions.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Linhh on 16/12/1.
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ActivityProtocol {
    String[] value() default "";
}
