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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合注解<br>
 * 作用：声明字段的类型为聚合<br>
 *
 * <p>
 * 解释：聚合一般指多个实体组装后的集合<br>
 * </p>
 *
 * <p>
 * 使用说明：<br>
 * 1、在使用上，聚合注解基本等效于实体注解<br>
 * 2、在不指定仓储的情况下，框架会自动为实体匹配仓储<br>
 * </p>
 *
 * <p>
 * 例如：<br>
 * <pre>{@code
 * @Aggregate
 * private List<User> users;
 *
 * 等效于：
 *
 * @Entity(repository = UserRepository.class)
 * private List<User> users;
 * }</pre>
 * </p>
 *
 * @author tao.chen
 */
@Entity
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    String name() default "";

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    Class<?> source() default Object.class;

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    Class<?> factory() default Object.class;

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

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    String sortBy() default "";

    /**
     * @see Entity
     */
    @AliasFor(annotation = Entity.class)
    String order() default "";

}
