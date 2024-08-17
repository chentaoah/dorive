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

package com.gitee.dorive.event.impl.listener.executor;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.api.entity.event.def.EventListenerDef;
import com.gitee.dorive.event.entity.CommonEvent;
import com.gitee.dorive.event.entity.executor.EntityEvent;
import com.gitee.dorive.event.entity.ExecutorEvent;
import com.gitee.dorive.event.impl.executor.EventExecutor;
import com.gitee.dorive.event.impl.listener.AbstractEventListener;
import com.gitee.dorive.event.impl.listener.EntityListenerAdapter;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;

public class ExecutorEventListener extends AbstractEventListener implements ApplicationListener<ExecutorEvent> {

    @Override
    protected Class<?> getBeanType() {
        return EntityEventListener.class;
    }

    @Override
    protected EntityListenerAdapter newAdapter(Integer order, EventListenerDef eventListenerDef, Object bean) {
        EntityEventListener listener = (EntityEventListener) bean;
        return new EntityListenerAdapter(order, eventListenerDef, bean, event -> listener.onEntityEvent((EntityEvent) event));
    }

    @Override
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        if (isHandle(executorEvent)) {
            Class<?> entityClass = executorEvent.getEntityClass();
            List<EntityListenerAdapter> existAdapters = getClassEntityListenerAdaptersMap().get(entityClass);
            if (existAdapters != null && !existAdapters.isEmpty()) {
                List<CommonEvent> commonEvents = convert(executorEvent);
                for (EntityListenerAdapter adapter : existAdapters) {
                    for (CommonEvent commonEvent : commonEvents) {
                        adapter.onCommonEvent(commonEvent);
                    }
                }
            }
        }
    }

    private List<CommonEvent> convert(ExecutorEvent executorEvent) {
        List<?> entities = executorEvent.getEntities();
        List<CommonEvent> commonEvents = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            EntityEvent entityEvent = new EntityEvent((EventExecutor) executorEvent.getSource());
            BeanUtil.copyProperties(executorEvent, entityEvent);
            entityEvent.setEntity(entity);
            commonEvents.add(entityEvent);
        }
        return commonEvents;
    }

}
