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
 * 绑定注解<br>
 * 作用：声明实体字段之间的绑定关系<br>
 *
 * <p>
 * 绑定类型有三种，分别是：<br>
 * 1、强绑定（两个字段强相关时使用）<br>
 * <pre>{@code
 * @Binding(field = "field", bindExp = "./field")
 * }</pre>
 * 2、弱绑定（字段与上下文相关时使用）<br>
 * <pre>{@code
 * @Binding(field = "field", processExp = "#ctx['field']")
 * }</pre>
 * 3、值绑定（字段与字面值相关时使用）<br>
 * <pre>{@code
 * @Binding(value = "0", bindExp = "./field")
 * }</pre>
 * </p>
 *
 * <p>
 * 当绑定的字段，还需要再进行深度解析时，请使用以下配置方式：<br>
 * <pre>{@code
 * @Binding(field = "field", bindExp = "./list", processExp = "#val.![field]", bindField = "field")
 * }</pre>
 * </p>
 *
 * @author tao.chen
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding {

    /**
     * 字段名称<br>
     * 作用：声明当前实体绑定的字段<br>
     */
    String field() default "";

    /**
     * 字面值<br>
     * 作用：值绑定时使用<br>
     */
    String value() default "";

    /**
     * 绑定表达式<br>
     * 作用：声明上下文绑定的字段<br>
     * 说明：<br>
     * 1、./field是以当前实体为参考系的相对路径<br>
     * 2、/field是以聚合根为参考系的绝对路径<br>
     */
    String bindExp() default "";

    /**
     * 加工表达式<br>
     * 作用：从上下文中获取信息，或加工绑定的字段<br>
     * 说明：表达式书写请参考SpEL<br>
     *
     * @see org.springframework.expression
     */
    String processExp() default "";

    /**
     * 指定加工器<br>
     * 作用：声明字段加工的具体实现<br>
     * 说明：如果加工表达式不为空，默认实现为SpELProcessor<br>
     */
    Class<?> processor() default Object.class;

    /**
     * 绑定的字段名称<br>
     * 作用：显式声明绑定的字段名称<br>
     */
    String bindField() default "";

}
