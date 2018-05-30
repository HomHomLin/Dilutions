package com.linhonghong.dilutions.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Linhh on 16/12/23.
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ProtocolFrom {
    String value() default "";
}
