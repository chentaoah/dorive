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
import com.gitee.dorive.core.repository.DefaultRepository;
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
    private String[] scenes;
    private Class<?> source;
    private Class<?> factory;
    private Class<?> repository;
    private int priority;
    private String orderByAsc;
    private String orderByDesc;
    private String builderKey;
    private String commandKey;

    public static EntityDef newEntityDefinition(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Entity.class)) {
            Map<String, Object> annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Entity.class);
            if (annotationAttributes != null) {
                Object factory = annotationAttributes.get("factory");
                if (factory == Object.class) {
                    annotationAttributes.put("factory", DefaultEntityFactory.class);
                }
                Object repository = annotationAttributes.get("repository");
                if (repository == Object.class) {
                    annotationAttributes.put("repository", DefaultRepository.class);
                }
            }
            return BeanUtil.copyProperties(annotationAttributes, EntityDef.class);
        }
        return null;
    }

    public void merge(EntityDef entityDef) {
        if (StringUtils.isBlank(name)) {
            name = entityDef.getName();
        }
        if (scenes == null || scenes.length == 0) {
            scenes = entityDef.getScenes();
        }
        if (source == null || source == Object.class) {
            source = entityDef.getSource();
        }
        if (factory == null || factory == DefaultEntityFactory.class) {
            factory = entityDef.getFactory();
        }
    }

}
