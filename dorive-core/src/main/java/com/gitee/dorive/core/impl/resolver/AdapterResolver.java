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

import com.gitee.dorive.api.entity.def.AdapterDef;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.core.api.Adapter;
import com.gitee.dorive.core.impl.adapter.DefaultAdapter;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
public class AdapterResolver {

    private AbstractContextRepository<?, ?> repository;
    private Adapter adapter;

    public AdapterResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        resolve();
    }

    public void resolve() {
        EntityType entityType = repository.getEntityType();
        AdapterDef adapterDef = entityType.getAdapterDef();
        if (adapterDef != null) {
            Class<?> adapterClass = adapterDef.getAdapter();
            if (adapterClass == Object.class) {
                adapter = new DefaultAdapter();
            } else {
                ApplicationContext applicationContext = repository.getApplicationContext();
                adapter = (Adapter) applicationContext.getBean(adapterClass);
            }
            if (adapter instanceof DefaultAdapter) {
                DefaultAdapter defaultAdapter = (DefaultAdapter) adapter;
                defaultAdapter.setAdapterDef(adapterDef);
            }
        }
    }

    public boolean isAdaptive() {
        return adapter != null;
    }

}
