/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 值对象
 */
@Field
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueObject {

    /**
     * @see Field
     */
    @AliasFor(annotation = Field.class)
    String value() default "";

    /**
     * @see Field
     */
    @AliasFor(annotation = Field.class)
    boolean valueObj() default true;

    /**
     * @see Field
     */
    @AliasFor(annotation = Field.class)
    Class<?> converter() default Object.class;

}
