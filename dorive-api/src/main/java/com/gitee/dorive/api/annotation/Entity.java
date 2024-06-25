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
     * 数据来源
     */
    Class<?> source() default Object.class;

    /**
     * 实体工厂
     */
    Class<?> factory() default Object.class;

    /**
     * 指定仓储
     */
    Class<?> repository() default Object.class;

    /**
     * 操作优先级
     */
    int priority() default 0;

    /**
     * 是否聚合
     */
    boolean aggregate() default false;

}

