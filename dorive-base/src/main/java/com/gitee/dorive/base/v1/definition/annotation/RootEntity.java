/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 关联根实体
 */
@Entity
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RootEntity {

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    String name() default "";

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    boolean aggregate() default false;

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    Class<?> repository() default Object.class;

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    int priority() default 0;

}
