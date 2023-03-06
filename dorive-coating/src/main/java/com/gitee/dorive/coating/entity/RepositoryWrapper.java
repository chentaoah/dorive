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

import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.coating.entity.definition.PropertyDef;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryWrapper {

    private MergedRepository mergedRepository;
    private List<PropertyWrapper> collectedPropertyWrappers;

    public Example newExampleByCoating(Context context, Object coating) {
        Example example = new Example();
        for (PropertyWrapper propertyWrapper : collectedPropertyWrappers) {
            Property property = propertyWrapper.getProperty();
            Object fieldValue = property.getFieldValue(coating);
            if (fieldValue != null) {
                PropertyDef propertyDef = propertyWrapper.getPropertyDef();
                String field = propertyDef.getField();
                String operator = propertyDef.getOperator();
                example.addCriterion(new Criterion(field, operator, fieldValue));
            }
        }
        CommonRepository definedRepository = mergedRepository.getDefinedRepository();
        BinderResolver binderResolver = definedRepository.getBinderResolver();
        for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
            Object boundValue = contextBinder.getBoundValue(context, null);
            if (boundValue != null) {
                BindingDef bindingDef = contextBinder.getBindingDef();
                String field = bindingDef.getField();
                example.eq(field, boundValue);
            }
        }
        return example;
    }

}
