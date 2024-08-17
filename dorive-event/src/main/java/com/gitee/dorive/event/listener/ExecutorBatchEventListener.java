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
import com.gitee.dorive.event.api.EntityBatchEventListener;
import com.gitee.dorive.api.entity.event.def.EntityListenerDef;
import com.gitee.dorive.event.entity.CommonEvent;
import com.gitee.dorive.event.entity.EntityBatchEvent;
import com.gitee.dorive.event.entity.ExecutorEvent;
import com.gitee.dorive.event.executor.EventExecutor;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class ExecutorBatchEventListener extends AbstractEventListener implements ApplicationListener<ExecutorEvent> {

    @Override
    protected Class<?> getBeanType() {
        return EntityBatchEventListener.class;
    }

    @Override
    protected EntityListenerAdapter newAdapter(Integer order, EntityListenerDef entityListenerDef, Object bean) {
        EntityBatchEventListener listener = (EntityBatchEventListener) bean;
        return new EntityListenerAdapter(order, entityListenerDef, bean, event -> listener.onEntityBatchEvent((EntityBatchEvent) event));
    }

    @Override
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        if (isHandle(executorEvent)) {
            Class<?> entityClass = executorEvent.getEntityClass();
            List<EntityListenerAdapter> existAdapters = getClassEntityListenerAdaptersMap().get(entityClass);
            if (existAdapters != null && !existAdapters.isEmpty()) {
                CommonEvent commonEvent = convert(executorEvent);
                for (EntityListenerAdapter adapter : existAdapters) {
                    adapter.onCommonEvent(commonEvent);
                }
            }
        }
    }

    private CommonEvent convert(ExecutorEvent executorEvent) {
        EntityBatchEvent entityBatchEvent = new EntityBatchEvent((EventExecutor) executorEvent.getSource());
        BeanUtil.copyProperties(executorEvent, entityBatchEvent);
        entityBatchEvent.setEntities(executorEvent.getEntities());
        return entityBatchEvent;
    }

}
