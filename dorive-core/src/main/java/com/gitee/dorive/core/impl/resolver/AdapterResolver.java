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

import com.gitee.dorive.core.api.ContextAdapter;
import com.gitee.dorive.core.entity.definition.AdapterDefinition;
import com.gitee.dorive.core.impl.DefaultContextAdapter;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
public class AdapterResolver {

    private AbstractContextRepository<?, ?> repository;

    private ContextAdapter contextAdapter;

    public AdapterResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveAdapter() {
        Class<?> entityClass = repository.getEntityClass();
        AdapterDefinition adapterDefinition = AdapterDefinition.newAdapterDefinition(entityClass);
        if (adapterDefinition != null) {
            Class<?> adapterClass = adapterDefinition.getAdapter();
            if (adapterClass == DefaultContextAdapter.class) {
                contextAdapter = new DefaultContextAdapter();
            } else {
                ApplicationContext applicationContext = repository.getApplicationContext();
                contextAdapter = (ContextAdapter) applicationContext.getBean(adapterClass);
            }
            if (contextAdapter instanceof DefaultContextAdapter) {
                DefaultContextAdapter defaultContextAdapter = (DefaultContextAdapter) contextAdapter;
                defaultContextAdapter.setAdapterDefinition(adapterDefinition);
            }
        }
    }

    public boolean isAdaptive() {
        return contextAdapter != null;
    }

}
