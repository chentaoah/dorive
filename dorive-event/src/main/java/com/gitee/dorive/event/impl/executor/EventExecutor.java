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

package com.gitee.dorive.event.impl.executor;

import com.gitee.dorive.api.constant.event.Publisher;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.impl.executor.AbstractProxyExecutor;
import com.gitee.dorive.event.entity.EntityEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

@Getter
@Setter
public class EventExecutor extends AbstractProxyExecutor {

    private ApplicationContext applicationContext;
    private EntityElement entityElement;

    public EventExecutor(Executor executor, ApplicationContext applicationContext, EntityElement entityElement) {
        super(executor);
        this.applicationContext = applicationContext;
        this.entityElement = entityElement;
    }

    @Override
    public int execute(Context context, Operation operation) {
        int totalCount = super.execute(context, operation);
        if (totalCount != 0) {
            if (operation instanceof EntityOp) {
                Class<?> entityClass = getEntityElement().getGenericType();
                EntityOp entityOp = (EntityOp) operation;

                EntityEvent entityEvent = new EntityEvent(this);
                entityEvent.setPublisher(Publisher.EXECUTOR);
                entityEvent.setEntityClass(entityClass);
                entityEvent.setName(EntityEvent.getEventName(entityOp));
                entityEvent.setContext(context);
                entityEvent.setRoot(entityOp.isUncontrolled());
                entityEvent.setEntities(entityOp.getEntities());

                getApplicationContext().publishEvent(entityEvent);
            }
        }
        return totalCount;
    }

}
