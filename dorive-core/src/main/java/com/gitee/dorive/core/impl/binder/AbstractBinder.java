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
package com.gitee.dorive.core.impl.binder;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.definition.BindingDefinition;
import com.gitee.dorive.core.api.Binder;
import com.gitee.dorive.core.api.Processor;
import com.gitee.dorive.core.entity.element.PropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractBinder implements Binder, Processor {

    protected BindingDefinition bindingDefinition;
    protected PropertyChain fieldPropertyChain;
    protected Processor processor;
    protected String alias;

    @Override
    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    @Override
    public Object getFieldValue(Context context, Object entity) {
        return fieldPropertyChain.getValue(entity);
    }

    @Override
    public void setFieldValue(Context context, Object entity, Object property) {
        fieldPropertyChain.setValue(entity, property);
    }

    @Override
    public Object input(Context context, Object valueObject) {
        return processor.input(context, valueObject);
    }

    @Override
    public Object output(Context context, Object valueObject) {
        return processor.output(context, valueObject);
    }

}
