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
package com.gitee.dorive.coating.entity;

import com.gitee.dorive.coating.entity.definition.PropertyDefinition;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.Property;
import com.gitee.dorive.core.entity.definition.BindingDefinition;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryWrapper {

    private MergedRepository mergedRepository;
    private List<PropertyWrapper> collectedPropertyWrappers;

    public Example newExampleByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = new Example();
        for (PropertyWrapper propertyWrapper : collectedPropertyWrappers) {
            Property property = propertyWrapper.getProperty();
            Object fieldValue = property.getFieldValue(coatingObject);
            if (fieldValue != null) {
                PropertyDefinition propertyDefinition = propertyWrapper.getPropertyDefinition();
                String alias = propertyDefinition.getAlias();
                String operator = propertyDefinition.getOperator();
                example.addCriterion(new Criterion(alias, operator, fieldValue));
            }
        }
        ConfiguredRepository definedRepository = mergedRepository.getDefinedRepository();
        BinderResolver binderResolver = definedRepository.getBinderResolver();
        for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
            Object boundValue = contextBinder.getBoundValue(boundedContext, null);
            if (boundValue != null) {
                BindingDefinition bindingDefinition = contextBinder.getBindingDefinition();
                String alias = bindingDefinition.getAlias();
                example.eq(alias, boundValue);
            }
        }
        return example;
    }

}
