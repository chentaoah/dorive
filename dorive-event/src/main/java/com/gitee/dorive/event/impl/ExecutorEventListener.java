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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.event.annotation.EntityListener;
import com.gitee.dorive.event.api.EntityBatchEventListener;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.CommonEvent;
import com.gitee.dorive.event.entity.EntityBatchEvent;
import com.gitee.dorive.event.entity.EntityEvent;
import com.gitee.dorive.event.entity.EntityListenerDef;
import com.gitee.dorive.event.entity.ExecutorEvent;
import com.gitee.dorive.event.enums.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.OrderUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Slf4j
public class ExecutorEventListener implements ApplicationListener<ExecutorEvent>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<Class<?>, List<EntityListenerAdapter>> classEntityListenerAdaptersMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EntityListener.class);
        List<Object> beans = new ArrayList<>(beanMap.values());
        AnnotationAwareOrderComparator.sort(beans);
        for (Object bean : beans) {
            if (checkBeanType(bean)) {
                Class<?> listenerType = bean.getClass();
                Integer order = OrderUtils.getOrder(listenerType, LOWEST_PRECEDENCE);
                EntityListenerDef entityListenerDef = EntityListenerDef.fromElement(listenerType);
                registry(order, entityListenerDef, bean);
            }
        }
    }

    private boolean checkBeanType(Object bean) {
        return bean instanceof EntityBatchEventListener || bean instanceof EntityEventListener;
    }

    public void registry(Integer order, EntityListenerDef entityListenerDef, Object bean) {
        if (entityListenerDef == null) {
            return;
        }
        Class<?> entityClass = entityListenerDef.getValue();
        if (entityClass == null) {
            return;
        }
        if (bean instanceof EntityBatchEventListener) {
            EntityBatchEventListener listener = (EntityBatchEventListener) bean;
            EntityListenerAdapter adapter = new EntityListenerAdapter(order, entityListenerDef, bean, event ->
                    listener.onEntityBatchEvent(newEntityBatchEvent(event)));
            List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
            existAdapters.add(adapter);
        }
        if (bean instanceof EntityEventListener) {
            EntityEventListener listener = (EntityEventListener) bean;
            EntityListenerAdapter adapter = new EntityListenerAdapter(order, entityListenerDef, bean, event -> {
                List<EntityEvent> entityEvents = newEntityEvents(event);
                for (EntityEvent entityEvent : entityEvents) {
                    listener.onEntityEvent(entityEvent);
                }
            });
            List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
            existAdapters.add(adapter);
        }
    }

    public void cancel(Class<?> entityClass, Object bean) {
        List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.get(entityClass);
        if (existAdapters == null) {
            return;
        }
        Collection<EntityListenerAdapter> filterAdapters = CollUtil.filterNew(existAdapters, adapter -> adapter.getBean() == bean);
        if (filterAdapters != null && !filterAdapters.isEmpty()) {
            existAdapters.removeAll(filterAdapters);
        }
    }

    @Override
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        EventExecutor eventExecutor = (EventExecutor) executorEvent.getSource();
        EntityEle entityEle = eventExecutor.getEntityEle();
        Class<?> entityClass = entityEle.getGenericType();
        List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.get(entityClass);
        if (existAdapters != null && !existAdapters.isEmpty()) {
            CommonEvent commonEvent = newCommonEvent(executorEvent);
            if (commonEvent != null) {
                for (EntityListenerAdapter adapter : existAdapters) {
                    adapter.onCommonEvent(commonEvent);
                }
            }
        }
    }

    private CommonEvent newCommonEvent(ExecutorEvent executorEvent) {
        Context context = executorEvent.getContext();
        Operation operation = executorEvent.getOperation();
        if (operation instanceof EntityOp) {
            OperationType operationType = OperationType.UNKNOWN;
            if (operation instanceof Insert) {
                operationType = OperationType.INSERT;

            } else if (operation instanceof Update) {
                operationType = OperationType.UPDATE;

            } else if (operation instanceof Delete) {
                operationType = OperationType.DELETE;
            }
            return new CommonEvent(executorEvent, context, operationType);
        }
        return null;
    }

    private EntityBatchEvent newEntityBatchEvent(CommonEvent commonEvent) {
        ExecutorEvent executorEvent = commonEvent.getExecutorEvent();
        EventExecutor eventExecutor = (EventExecutor) executorEvent.getSource();
        Operation operation = executorEvent.getOperation();

        EntityEle entityEle = eventExecutor.getEntityEle();
        Class<?> entityClass = entityEle.getGenericType();

        EntityOp entityOp = (EntityOp) operation;
        List<?> entities = entityOp.getEntities();

        EntityBatchEvent entityBatchEvent = BeanUtil.copyProperties(commonEvent, EntityBatchEvent.class);
        entityBatchEvent.setEntityClass(entityClass);
        entityBatchEvent.setEntities(entities);
        return entityBatchEvent;
    }

    private List<EntityEvent> newEntityEvents(CommonEvent commonEvent) {
        ExecutorEvent executorEvent = commonEvent.getExecutorEvent();
        Operation operation = executorEvent.getOperation();

        EntityOp entityOp = (EntityOp) operation;
        List<?> entities = entityOp.getEntities();

        List<EntityEvent> entityEvents = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            EntityEvent entityEvent = BeanUtil.copyProperties(commonEvent, EntityEvent.class);
            entityEvent.setEntity(entity);
            entityEvents.add(entityEvent);
        }
        return entityEvents;
    }

}
