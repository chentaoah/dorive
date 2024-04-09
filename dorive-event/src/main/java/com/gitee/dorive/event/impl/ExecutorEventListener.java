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

package com.gitee.dorive.event.impl;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.EntityEvent;
import com.gitee.dorive.event.entity.EntityListenerDef;
import com.gitee.dorive.event.entity.ExecutorEvent;
import com.gitee.dorive.event.entity.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.OrderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Slf4j
public class ExecutorEventListener implements ApplicationListener<ExecutorEvent>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<Class<?>, List<EntityEventListener>> classEntityEventListenersMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EntityEventListener> entityEventListenerMap = applicationContext.getBeansOfType(EntityEventListener.class);
        List<EntityEventListener> entityEventListeners = new ArrayList<>(entityEventListenerMap.values());
        AnnotationAwareOrderComparator.sort(entityEventListeners);

        for (EntityEventListener entityEventListener : entityEventListeners) {
            Class<?> listenerType = entityEventListener.getClass();
            Integer order = OrderUtils.getOrder(listenerType, LOWEST_PRECEDENCE);
            EntityListenerDef entityListenerDef = EntityListenerDef.fromElement(listenerType);
            registry(order, entityListenerDef, entityEventListener);
        }
    }

    public void registry(Integer order, EntityListenerDef entityListenerDef, EntityEventListener entityEventListener) {
        if (entityListenerDef == null) {
            return;
        }
        Class<?> entityClass = entityListenerDef.getValue();
        if (entityClass == null) {
            return;
        }
        if (entityEventListener instanceof EntityEventListenerAdapter) {
            EntityEventListenerAdapter entityEventListenerAdapter = (EntityEventListenerAdapter) entityEventListener;
            entityEventListenerAdapter.setOrder(order);
            entityEventListenerAdapter.setEntityListenerDef(entityListenerDef);

        } else {
            entityEventListener = new EntityEventListenerAdapter(order, entityListenerDef, entityEventListener);
        }
        List<EntityEventListener> existEntityEventListeners = classEntityEventListenersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
        existEntityEventListeners.add(entityEventListener);
    }

    public void cancel(Class<?> entityClass, EntityEventListener entityEventListener) {
        List<EntityEventListener> existEntityEventListeners = classEntityEventListenersMap.get(entityClass);
        if (existEntityEventListeners == null) {
            return;
        }
        EntityEventListener existEntityEventListener = CollUtil.findOne(existEntityEventListeners,
                listener -> entityEventListener == getRealListener(listener));
        if (existEntityEventListener != null) {
            existEntityEventListeners.remove(existEntityEventListener);
        }
    }

    private EntityEventListener getRealListener(EntityEventListener entityEventListener) {
        if (entityEventListener instanceof EntityEventListenerAdapter) {
            return ((EntityEventListenerAdapter) entityEventListener).getEntityEventListener();
        }
        return entityEventListener;
    }

    @Override
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        EventExecutor eventExecutor = (EventExecutor) executorEvent.getSource();
        EntityEle entityEle = eventExecutor.getEntityEle();
        Class<?> entityClass = entityEle.getGenericType();
        List<EntityEventListener> entityEventListeners = classEntityEventListenersMap.get(entityClass);
        if (entityEventListeners != null && !entityEventListeners.isEmpty()) {
            EntityEvent entityEvent = newEntityEvent(executorEvent);
            for (EntityEventListener entityEventListener : entityEventListeners) {
                entityEventListener.onEntityEvent(entityEvent);
            }
        }
    }

    private EntityEvent newEntityEvent(ExecutorEvent executorEvent) {
        Context context = executorEvent.getContext();
        Operation operation = executorEvent.getOperation();
        OperationType operationType = OperationType.UNKNOWN;
        if (operation instanceof Insert) {
            operationType = OperationType.INSERT;

        } else if (operation instanceof Update) {
            operationType = OperationType.UPDATE;

        } else if (operation instanceof Delete) {
            operationType = OperationType.DELETE;
        }
        Object entity = operation.getEntity();
        return new EntityEvent(executorEvent, context, operationType, entity);
    }

}
