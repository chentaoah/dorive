/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 仓储
 */
@Component
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 数据源
     */
    Class<?> dataSource();

    /**
     * 实体工厂
     */
    Class<?> factory() default Object.class;

    /**
     * 反序列化
     */
    Class<?> deserializer() default Object.class;

    /**
     * 序列化
     */
    Class<?> serializer() default Object.class;

    /**
     * 派生
     */
    Class<?>[] derived() default {};

    /**
     * 事件
     */
    Event[] events() default {};

    /**
     * 查询对象
     */
    Class<?>[] queries() default {};

}
