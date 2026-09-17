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
