/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import java.lang.annotation.*;

/**
 * 绑定
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding {

    /**
     * 子实体源字段
     */
    String source() default "";

    /**
     * 字面值
     */
    String literal() default "";

    /**
     * 父实体目标字段
     */
    String target() default "";

    /**
     * 加工表达式
     */
    String expression() default "";

    /**
     * 指定加工器
     */
    Class<?> processor() default Object.class;

    /**
     * 当目标字段为实体时，目标实体的字段
     */
    String targetField() default "";

}
