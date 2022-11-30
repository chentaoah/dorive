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
package com.gitee.dorive.core.impl.resolver;

import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.definition.SelectorDefinition;
import com.gitee.dorive.core.impl.DefaultSelector;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
public class SelectorResolver {

    private AbstractContextRepository<?, ?> repository;

    private Selector selector;

    public SelectorResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveSelector() {
        Class<?> entityClass = repository.getEntityClass();
        SelectorDefinition selectorDefinition = SelectorDefinition.newSelectorDefinition(entityClass);
        if (selectorDefinition != null) {
            Class<?> selectorClass = selectorDefinition.getSelector();
            if (selectorClass == DefaultSelector.class) {
                selector = new DefaultSelector();
            } else {
                ApplicationContext applicationContext = repository.getApplicationContext();
                selector = (Selector) applicationContext.getBean(selectorClass);
            }
            if (selector instanceof DefaultSelector) {
                DefaultSelector defaultSelector = (DefaultSelector) selector;
                defaultSelector.setSelectorDefinition(selectorDefinition);
            }
        }
    }

    public boolean isSelectable() {
        return selector != null;
    }

}
