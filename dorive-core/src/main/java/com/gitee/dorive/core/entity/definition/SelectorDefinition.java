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
import com.gitee.dorive.core.annotation.Selector;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
@AllArgsConstructor
public class SelectorDefinition {

    private Class<?> selector;
    private String[] args;

    public static SelectorDefinition newSelectorDefinition(AnnotatedElement annotatedElement) {
        Selector selectorAnnotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, Selector.class);
        if (selectorAnnotation != null) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(selectorAnnotation);
            return BeanUtil.copyProperties(annotationAttributes, SelectorDefinition.class);
        }
        return null;
    }

}
