/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import java.lang.annotation.*;

/**
 * 实体
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    /**
     * 实体名称
     */
    String name() default "";

    /**
     * 是否聚合
     */
    boolean aggregate() default true;

    /**
     * 指定仓储
     */
    Class<?> repository() default Object.class;

    /**
     * 操作优先级
     */
    int priority() default 0;

}

