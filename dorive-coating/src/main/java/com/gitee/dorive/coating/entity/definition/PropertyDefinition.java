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
package com.gitee.dorive.coating.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.coating.annotation.Property;
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
public class PropertyDefinition {

    private String belongTo;
    private String alias;
    private String operator;
    private boolean ignore;

    public static PropertyDefinition newPropertyDefinition(AnnotatedElement annotatedElement) {
        Map<String, Object> annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Property.class);
        if (annotationAttributes == null) {
            return new PropertyDefinition("", "", "=", false);
        }
        return BeanUtil.copyProperties(annotationAttributes, PropertyDefinition.class);
    }

    public static void renewPropertyDefinition(String fieldName, PropertyDefinition propertyDefinition) {
        if (StringUtils.isBlank(propertyDefinition.getAlias())) {
            propertyDefinition.setAlias(fieldName);
        }
    }

}
