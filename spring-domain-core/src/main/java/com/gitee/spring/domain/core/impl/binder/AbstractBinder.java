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
package com.gitee.spring.domain.core.impl.binder;

import com.gitee.spring.domain.core.api.Binder;
import com.gitee.spring.domain.core.api.Processor;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractBinder implements Binder, Processor {

    protected BindingDefinition bindingDefinition;
    protected PropertyChain fieldPropertyChain;
    protected Processor processor;

    @Override
    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    @Override
    public Object getFieldValue(BoundedContext boundedContext, Object entity) {
        return fieldPropertyChain.getValue(entity);
    }

    @Override
    public void setFieldValue(BoundedContext boundedContext, Object entity, Object property) {
        fieldPropertyChain.setValue(entity, property);
    }

    @Override
    public Object input(BoundedContext boundedContext, Object value) {
        return processor.input(boundedContext, value);
    }

    @Override
    public Object output(BoundedContext boundedContext, Object value) {
        return processor.output(boundedContext, value);
    }

}
