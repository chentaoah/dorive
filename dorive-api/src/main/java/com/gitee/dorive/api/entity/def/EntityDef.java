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
package com.gitee.dorive.api.entity.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.api.annotation.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDef {

    private String name;
    private Class<?> source;
    private Class<?> factory;
    private Class<?> repository;
    private int priority;
    private String sortBy;
    private String order;

    public static EntityDef fromElement(AnnotatedElement element) {
        if (element.isAnnotationPresent(Entity.class)) {
            Map<String, Object> attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(element, Entity.class);
            return BeanUtil.copyProperties(attributes, EntityDef.class);
        }
        return null;
    }

    public void merge(EntityDef entityDef) {
        if (StringUtils.isBlank(name)) {
            name = entityDef.getName();
        }
        if (source == null || source == Object.class) {
            source = entityDef.getSource();
        }
        if (factory == null || factory == Object.class) {
            factory = entityDef.getFactory();
        }
    }

    public boolean isAggregated() {
        return repository != Object.class;
    }

}
