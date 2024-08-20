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
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.OrderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Data
public abstract class AbstractEventListener implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<Class<?>, List<EntityEventListener>> classAdaptersMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, ?> beanMap = applicationContext.getBeansOfType(getBeanType());
        List<Object> beans = new ArrayList<>(beanMap.values());
        AnnotationAwareOrderComparator.sort(beans);
        for (Object bean : beans) {
            Class<?> listenerType = bean.getClass();
            Integer order = OrderUtils.getOrder(listenerType, LOWEST_PRECEDENCE);
            ListenerDef listenerDef = ListenerDef.fromElement(listenerType);
            registry(order, listenerDef, bean);
        }
    }

    public void registry(Integer order, ListenerDef listenerDef, Object bean) {
        if (listenerDef == null) {
            return;
        }
        Class<?> entityClass = listenerDef.getEntityClass();
        if (entityClass == null) {
            return;
        }
        EntityEventListener adapter = newAdapter(order, listenerDef, bean);
        List<EntityEventListener> existAdapters = classAdaptersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
        existAdapters.add(adapter);
    }

    protected abstract Class<?> getBeanType();

    protected abstract EntityEventListener newAdapter(Integer order, ListenerDef listenerDef, Object bean);

}
