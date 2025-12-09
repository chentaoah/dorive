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

import com.gitee.dorive.api.entity.core.def.BindingDef;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.impl.endpoint.BindEndpoint;
import com.gitee.dorive.core.impl.endpoint.FieldEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractBinder implements Binder {

    protected BindingDef bindingDef;
    protected FieldEndpoint fieldEndpoint;
    protected BindEndpoint bindEndpoint;
    protected Processor processor;

    public String getFieldName() {
        return fieldEndpoint.getFieldDefinition().getFieldName();
    }

    public Object getFieldValue(Context context, Object entity) {
        return fieldEndpoint.getValue(entity);
    }

    public void setFieldValue(Context context, Object entity, Object value) {
        fieldEndpoint.setValue(entity, value);
    }

    public String getBoundName() {
        return bindEndpoint.getFieldDefinition().getFieldName();
    }

    public Object getBoundValue(Context context, Object entity) {
        return bindEndpoint.getValue(entity);
    }

    public void setBoundValue(Context context, Object entity, Object value) {
        bindEndpoint.setValue(entity, value);
    }

    @Override
    public Object input(Context context, Object value) {
        return value == null || processor == null ? value : processor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return value == null || processor == null ? value : processor.output(context, value);
    }

    public String getBindField() {
        return bindingDef.getBindField();
    }

    public boolean isBindCollection() {
        return bindEndpoint.getFieldDefinition().isCollection();
    }

    public String getBelongAccessPath() {
        return bindEndpoint.getBelongAccessPath();
    }

    public boolean isSameType() {
        return fieldEndpoint.getFieldDefinition().isSameType(bindEndpoint.getFieldDefinition());
    }

}
