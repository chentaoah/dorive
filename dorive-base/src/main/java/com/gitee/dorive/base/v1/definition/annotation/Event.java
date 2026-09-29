/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件
 */
@Inherited
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Event {

    /**
     * 源事件
     */
    @AliasFor("source")
    Class<?> value() default Object.class;

    /**
     * 源事件
     */
    @AliasFor("value")
    Class<?> source() default Object.class;

    /**
     * 目标事件
     */
    Class<?> target() default Object.class;

    /**
     * 发布
     */
    Class<?> publisher() default Object.class;

}
