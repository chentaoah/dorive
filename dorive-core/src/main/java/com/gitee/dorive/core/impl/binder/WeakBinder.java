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
import com.gitee.dorive.api.impl.PropChain;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;

public class WeakBinder extends FieldBinder {

    public WeakBinder(BindingDef bindingDef, Processor processor, PropChain fieldPropChain, String alias) {
        super(bindingDef, processor, fieldPropChain, alias);
    }

    @Override
    public Object input(Context context, Object value) {
        return processor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return processor.output(context, value);
    }

}
