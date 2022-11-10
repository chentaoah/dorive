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
package com.gitee.spring.domain.core.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BindingDefinition {

    private String field;
    private String bindProp;
    private String bindCtx;
    private String alias;
    private String bindAlias;

    public static List<BindingDefinition> newBindingDefinitions(ElementDefinition elementDefinition) {
        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        for (Binding bindingAnnotation : elementDefinition.getBindingAnnotations()) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation);
            bindingDefinitions.add(BeanUtil.copyProperties(annotationAttributes, BindingDefinition.class));
        }
        return bindingDefinitions;
    }

}
