/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import java.lang.annotation.*;

/**
 * 排序
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderBy {

    /**
     * 排序字段
     */
    String field() default "";

    /**
     * 排序方式
     */
    String sort() default "";

}

