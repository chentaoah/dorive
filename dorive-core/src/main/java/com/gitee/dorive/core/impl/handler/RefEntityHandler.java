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
package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.api.Ref;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.element.EntityElement;
import com.gitee.dorive.core.impl.DefaultRef;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;

import java.util.List;

public class RefEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public RefEntityHandler(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        if (boundedContext.isRef()) {
            EntityElement entityElement = repository.getEntityElement();
            PropertyProxy refProxy = entityElement.getRefProxy();
            if (refProxy != null) {
                for (Object rootEntity : rootEntities) {
                    Ref ref = new DefaultRef((AbstractRepository<Object, Object>) repository, entityHandler, boundedContext, rootEntities, rootEntity);
                    refProxy.setValue(rootEntity, ref);
                }
            }
        }
        return entityHandler.handleEntities(boundedContext, rootEntities);
    }

}
