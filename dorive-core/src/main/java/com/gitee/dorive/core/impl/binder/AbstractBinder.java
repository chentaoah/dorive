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

import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.BindingProcessor;
import com.gitee.dorive.core.api.context.Context;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public abstract class AbstractBinder implements Binder, BindingProcessor {

    private BindingDef bindingDef;
    private String alias;
    private PropChain fieldPropChain;
    private BindingProcessor bindingProcessor;

    public String getFieldName() {
        return fieldPropChain.getEntityField().getName();
    }

    @Override
    public Object getFieldValue(Context context, Object entity) {
        return fieldPropChain.getValue(entity);
    }

    @Override
    public void setFieldValue(Context context, Object entity, Object property) {
        fieldPropChain.setValue(entity, property);
    }

    @Override
    public Object input(Context context, Object value) {
        return bindingProcessor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return bindingProcessor.output(context, value);
    }

    public List<Object> collectFieldValues(Context context, List<Object> entities) {
        List<Object> fieldValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object fieldValue = getFieldValue(context, entity);
            if (fieldValue != null) {
                fieldValue = output(context, fieldValue);
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

}
