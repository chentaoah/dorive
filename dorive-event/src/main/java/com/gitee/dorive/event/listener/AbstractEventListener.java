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

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.api.entity.event.def.EntityListenerDef;
import com.gitee.dorive.event.entity.CommonEvent;
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
    private final Map<Class<?>, List<EntityListenerAdapter>> classEntityListenerAdaptersMap = new ConcurrentHashMap<>();

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
            EntityListenerDef entityListenerDef = EntityListenerDef.fromElement(listenerType);
            registry(order, entityListenerDef, bean);
        }
    }

    public void registry(Integer order, EntityListenerDef entityListenerDef, Object bean) {
        if (entityListenerDef == null) {
            return;
        }
        Class<?> entityClass = entityListenerDef.getValue();
        if (entityClass == null) {
            return;
        }
        EntityListenerAdapter adapter = newAdapter(order, entityListenerDef, bean);
        List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.computeIfAbsent(entityClass, key -> new ArrayList<>(4));
        existAdapters.add(adapter);
    }

    public void cancel(Class<?> entityClass, Object bean) {
        List<EntityListenerAdapter> existAdapters = classEntityListenerAdaptersMap.get(entityClass);
        if (existAdapters == null) {
            return;
        }
        EntityListenerAdapter adapter = CollUtil.findOne(existAdapters, a -> a.getBean() == bean);
        if (adapter != null) {
            existAdapters.remove(adapter);
        }
    }

    public boolean isHandle(CommonEvent commonEvent) {
        Operation operation = commonEvent.getOperation();
        return operation instanceof EntityOp;
    }

    protected abstract Class<?> getBeanType();

    protected abstract EntityListenerAdapter newAdapter(Integer order, EntityListenerDef entityListenerDef, Object bean);

}
