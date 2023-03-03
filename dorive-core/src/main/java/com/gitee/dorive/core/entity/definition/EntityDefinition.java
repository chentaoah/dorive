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
package com.gitee.dorive.core.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.api.annotation.Entity;
import com.gitee.dorive.core.impl.DefaultEntityFactory;
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
public class EntityDefinition {

    private String name;
    private String[] scenes;
    private int order;
    private Class<?> mapper;
    private Class<?> factory;
    private Class<?> repository;
    private String orderByAsc;
    private String orderByDesc;
    private String builderKey;
    private String commandKey;

    public static EntityDefinition newEntityDefinition(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Entity.class)) {
            Map<String, Object> annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Entity.class);
            return BeanUtil.copyProperties(annotationAttributes, EntityDefinition.class);
        }
        return null;
    }

    public void merge(EntityDefinition entityDefinition) {
        if (StringUtils.isBlank(name)) {
            name = entityDefinition.getName();
        }
        if (scenes == null || scenes.length == 0) {
            scenes = entityDefinition.getScenes();
        }
        if (mapper == null || mapper == Object.class) {
            mapper = entityDefinition.getMapper();
        }
        if (factory == null || factory == DefaultEntityFactory.class) {
            factory = entityDefinition.getFactory();
        }
    }

}
