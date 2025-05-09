/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.api.annotation.core;

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
     * 派生
     */
    Class<?>[] derived() default {};

    /**
     * 事件
     */
    Class<?>[] events() default {};

    /**
     * 查询对象
     */
    Class<?>[] queries() default {};

    /**
     * 边界上下文
     */
    String boundedContext() default "";

}
