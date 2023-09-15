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

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.event.api.EntityListener;
import com.gitee.dorive.event.entity.ExecutorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExecutorListener implements ApplicationListener<ExecutorEvent>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<Class<?>, List<EntityListener>> classEventListenersMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EntityListener> entityListenerMap = applicationContext.getBeansOfType(EntityListener.class);
        List<EntityListener> entityListeners = new ArrayList<>(entityListenerMap.values());
        entityListeners.sort(new AnnotationAwareOrderComparator());
        for (EntityListener entityListener : entityListeners) {
            Class<?> entityClass = entityListener.subscribe();
            if (entityClass != null) {
                List<EntityListener> existEntityListeners = classEventListenersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
                existEntityListeners.add(entityListener);
            }
        }
    }

    @Override
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        EventExecutor eventExecutor = (EventExecutor) executorEvent.getSource();
        EntityEle entityEle = eventExecutor.getEntityEle();
        Class<?> entityClass = entityEle.getGenericType();
        List<EntityListener> entityListeners = classEventListenersMap.get(entityClass);
        if (entityListeners != null && !entityListeners.isEmpty()) {
            for (EntityListener entityListener : entityListeners) {
                try {
                    entityListener.onApplicationEvent(executorEvent);
                } catch (Exception e) {
                    log.error("Exception occurred in event listening!", e);
                }
            }
        }
    }

}
