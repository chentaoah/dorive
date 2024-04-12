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

package com.gitee.dorive.event.listener;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.event.api.AggregateRootEventListener;
import com.gitee.dorive.event.def.EntityListenerDef;
import com.gitee.dorive.event.entity.AggregateRootEvent;
import com.gitee.dorive.event.entity.CommonEvent;
import com.gitee.dorive.event.entity.RepositoryEvent;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class RepositoryRootEventListener extends AbstractEventListener implements ApplicationListener<RepositoryEvent> {

    @Override
    protected Class<?> getBeanType() {
        return AggregateRootEventListener.class;
    }

    @Override
    protected EntityListenerAdapter newAdapter(Integer order, EntityListenerDef entityListenerDef, Object bean) {
        AggregateRootEventListener listener = (AggregateRootEventListener) bean;
        return new EntityListenerAdapter(order, entityListenerDef, bean, event -> listener.onAggregateRootEvent((AggregateRootEvent) event));
    }

    @Override
    public void onApplicationEvent(RepositoryEvent repositoryEvent) {
        if (isHandle(repositoryEvent)) {
            Class<?> entityClass = repositoryEvent.getEntityClass();
            List<EntityListenerAdapter> existAdapters = getClassEntityListenerAdaptersMap().get(entityClass);
            if (existAdapters != null && !existAdapters.isEmpty()) {
                CommonEvent commonEvent = convert(repositoryEvent);
                for (EntityListenerAdapter adapter : existAdapters) {
                    adapter.onCommonEvent(commonEvent);
                }
            }
        }
    }

    @Override
    public boolean isHandle(CommonEvent commonEvent) {
        Operation operation = commonEvent.getOperation();
        return super.isHandle(commonEvent) && operation.isUncontrolled();
    }

    private CommonEvent convert(RepositoryEvent repositoryEvent) {
        AggregateRootEvent aggregateRootEvent = new AggregateRootEvent((AbstractEventRepository<?, ?>) repositoryEvent.getSource());
        BeanUtil.copyProperties(repositoryEvent, aggregateRootEvent);
        aggregateRootEvent.setEntities(repositoryEvent.getEntities());
        return aggregateRootEvent;
    }

}
