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

package com.gitee.dorive.event.impl.listener;

import com.gitee.dorive.api.entity.event.def.ListenerDef;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.EntityEvent;
import com.gitee.dorive.event.impl.listener.adapter.RouteListenerAdapter;
import com.gitee.dorive.event.impl.listener.adapter.TransactionListenerAdapter;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class EventListener extends AbstractEventListener implements ApplicationListener<EntityEvent> {

    @Override
    protected Class<?> getBeanType() {
        return EntityEventListener.class;
    }

    @Override
    protected EntityEventListener newAdapter(Integer order, ListenerDef listenerDef, Object bean) {
        EntityEventListener listener = (EntityEventListener) bean;
        listener = new TransactionListenerAdapter(order, listenerDef, listener);
        listener = new RouteListenerAdapter(listenerDef, listener);
        return listener;
    }

    @Override
    public void onApplicationEvent(EntityEvent entityEvent) {
        Class<?> entityClass = entityEvent.getEntityClass();
        List<EntityEventListener> existAdapters = getClassAdaptersMap().get(entityClass);
        if (existAdapters != null && !existAdapters.isEmpty()) {
            for (EntityEventListener adapter : existAdapters) {
                adapter.onEntityEvent(entityEvent);
            }
        }
    }

}
