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
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.api.entity.element.PropChain;

import java.util.Map;

public class ContextBinder extends AbstractBinder {

    public ContextBinder(BindingDef bindingDef, String alias, PropChain fieldPropChain, Processor processor) {
        super(bindingDef, alias, fieldPropChain, processor);
    }

    @Override
    public Object getBoundValue(Context context, Object rootEntity) {
        Map<String, Object> attachments = context.getAttachments();
        String bindExp = getBindingDef().getBindExp();
        return attachments.get(bindExp);
    }

    @Override
    public void setBoundValue(Context context, Object rootEntity, Object property) {
        // ignore
    }

}
