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

import com.gitee.spring.domain.core.api.Processor;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyBinder extends AbstractBinder {

    protected String belongAccessPath;
    protected ConfiguredRepository belongRepository;
    protected PropertyChain boundPropertyChain;

    public PropertyBinder(BindingDefinition bindingDefinition,
                          PropertyChain fieldPropertyChain,
                          Processor processor,
                          String belongAccessPath,
                          ConfiguredRepository belongRepository,
                          PropertyChain boundPropertyChain) {
        super(bindingDefinition, fieldPropertyChain, processor);
        this.belongAccessPath = belongAccessPath;
        this.belongRepository = belongRepository;
        this.boundPropertyChain = boundPropertyChain;
    }

    public boolean isSameType() {
        return fieldPropertyChain.getProperty().isSameType(boundPropertyChain.getProperty());
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        return boundPropertyChain.getValue(rootEntity);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        boundPropertyChain.setValue(rootEntity, property);
    }

}
