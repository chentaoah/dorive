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
package com.gitee.spring.domain.event.impl;

import com.gitee.spring.domain.core.entity.EntityElement;
import com.gitee.spring.domain.event.annotation.Listener;
import com.gitee.spring.domain.event.api.EntityListener;
import com.gitee.spring.domain.event.entity.RepositoryEvent;
import com.gitee.spring.domain.event.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RepositoryListener implements ApplicationListener<RepositoryEvent>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<Class<?>, List<EntityListener>> classEventListenersMap = new LinkedHashMap<>();

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
            Listener listener = AnnotationUtils.getAnnotation(entityListener.getClass(), Listener.class);
            if (listener != null) {
                Class<?> entityClass = listener.value();
                List<EntityListener> existEntityListeners = classEventListenersMap.computeIfAbsent(entityClass, key -> new ArrayList<>());
                existEntityListeners.add(entityListener);
            }
        }
    }

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        EventRepository eventRepository = (EventRepository) event.getSource();
        EntityElement entityElement = eventRepository.getEntityElement();
        Class<?> entityClass = entityElement.getGenericEntityClass();
        List<EntityListener> entityListeners = classEventListenersMap.get(entityClass);
        if (entityListeners != null && !entityListeners.isEmpty()) {
            for (EntityListener entityListener : entityListeners) {
                try {
                    entityListener.onApplicationEvent(event);
                } catch (Exception e) {
                    log.error("Exception occurred in event listening!", e);
                }
            }
        }
    }

}
