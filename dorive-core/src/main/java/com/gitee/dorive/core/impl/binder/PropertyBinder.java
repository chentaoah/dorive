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
import com.gitee.dorive.core.api.common.Processor;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyBinder extends AbstractBinder {

    private String belongAccessPath;
    private CommonRepository belongRepository;
    private PropChain boundPropChain;
    private String bindAlias;

    public PropertyBinder(BindingDef bindingDef,
                          String alias,
                          PropChain fieldPropChain,
                          Processor processor,
                          String belongAccessPath,
                          CommonRepository belongRepository,
                          PropChain boundPropChain,
                          String bindAlias) {
        super(bindingDef, alias, fieldPropChain, processor);
        this.belongAccessPath = belongAccessPath;
        this.belongRepository = belongRepository;
        this.boundPropChain = boundPropChain;
        this.bindAlias = bindAlias;
    }

    public boolean isSameType() {
        return getFieldPropChain().isSameType(boundPropChain);
    }

    public String getBoundName() {
        return boundPropChain.getEntityField().getName();
    }

    @Override
    public Object getBoundValue(Context context, Object rootEntity) {
        return boundPropChain.getValue(rootEntity);
    }

    @Override
    public void setBoundValue(Context context, Object rootEntity, Object property) {
        boundPropChain.setValue(rootEntity, property);
    }

}
