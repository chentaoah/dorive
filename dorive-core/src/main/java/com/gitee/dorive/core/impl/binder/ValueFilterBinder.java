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

import cn.hutool.core.convert.Convert;
import com.gitee.dorive.api.def.BindingDef;
import com.gitee.dorive.api.entity.PropChain;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValueFilterBinder extends FieldBinder {

    private Object value;

    public ValueFilterBinder(BindingDef bindingDef, Processor processor, PropChain fieldPropChain, String alias) {
        super(bindingDef, processor, fieldPropChain, alias);
        Class<?> genericType = fieldPropChain.getEntityField().getGenericType();
        this.value = Convert.convert(genericType, bindingDef.getValue());
    }

    @Override
    public String getBoundName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getBoundValue(Context context, Object rootEntity) {
        return value;
    }

    @Override
    public void setBoundValue(Context context, Object rootEntity, Object property) {
        throw new UnsupportedOperationException();
    }

}
