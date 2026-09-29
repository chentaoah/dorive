/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {

    @AliasFor("name")
    String value() default "";

    String[] path() default {};

    Class<?> entity() default Object.class;

    @AliasFor("value")
    String name() default "";

    String field() default "";

    String operator() default "=";

}
