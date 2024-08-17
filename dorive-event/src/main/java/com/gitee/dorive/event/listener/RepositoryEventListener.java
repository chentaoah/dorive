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
import com.gitee.dorive.event.api.AggregateEventListener;
import com.gitee.dorive.api.entity.event.def.EntityListenerDef;
import com.gitee.dorive.event.entity.AggregateEvent;
import com.gitee.dorive.event.entity.CommonEvent;
import com.gitee.dorive.event.entity.RepositoryEvent;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class RepositoryEventListener extends AbstractEventListener implements ApplicationListener<RepositoryEvent> {

    @Override
    protected Class<?> getBeanType() {
        return AggregateEventListener.class;
    }

    @Override
    protected EntityListenerAdapter newAdapter(Integer order, EntityListenerDef entityListenerDef, Object bean) {
        AggregateEventListener listener = (AggregateEventListener) bean;
        return new EntityListenerAdapter(order, entityListenerDef, bean, event -> listener.onAggregateEvent((AggregateEvent) event));
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

    private CommonEvent convert(RepositoryEvent repositoryEvent) {
        AggregateEvent aggregateEvent = new AggregateEvent((AbstractEventRepository<?, ?>) repositoryEvent.getSource());
        BeanUtil.copyProperties(repositoryEvent, aggregateEvent);
        aggregateEvent.setEntities(repositoryEvent.getEntities());
        return aggregateEvent;
    }

}
