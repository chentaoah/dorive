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

package com.gitee.dorive.api.annotation;

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
