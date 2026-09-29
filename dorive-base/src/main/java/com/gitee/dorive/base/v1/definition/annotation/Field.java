/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 字段
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface Field {

    /**
     * 是否主键
     */
    boolean primary() default false;

    /**
     * 别名
     */
    @AliasFor("alias")
    String value() default "";

    /**
     * 别名
     */
    @AliasFor("value")
    String alias() default "";

    /**
     * 是否值对象
     */
    boolean valueObj() default false;

    /**
     * 映射表达式
     */
    String expression() default "";

    /**
     * 指定转换器
     */
    Class<?> converter() default Object.class;

}
