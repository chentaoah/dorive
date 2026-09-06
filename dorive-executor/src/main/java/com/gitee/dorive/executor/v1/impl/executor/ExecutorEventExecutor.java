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

package com.gitee.dorive.executor.v1.impl.executor;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.entity.op.EntityOp;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.event.api.EventFactory;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class ExecutorEventExecutor extends AbstractProxyExecutor {

    private final RepositoryContext repositoryContext;
    private final EntityElement entityElement;

    public ExecutorEventExecutor(RepositoryContext repositoryContext, EntityElement entityElement, Executor executor) {
        super(executor);
        this.repositoryContext = repositoryContext;
        this.entityElement = entityElement;
    }

    @Override
    public int execute(Context context, Operation operation) {
        int totalCount = super.execute(context, operation);
        if (totalCount != 0) {
            if (operation instanceof EntityOp entityOp) {
                EntityElement entityElement = getEntityElement();
                List<EventFactory> executorEventFactories = repositoryContext.getExecutorEventFactories();
                ApplicationContext applicationContext = repositoryContext.getApplicationContext();

                Class<?> entityClass = entityElement.getGenericType();
                for (EventFactory eventFactory : executorEventFactories) {
                    ApplicationEvent applicationEvent = eventFactory.newApplicationEvent(this, entityOp.isUncontrolled(), entityClass, context, entityOp);
                    if (applicationEvent != null) {
                        applicationContext.publishEvent(applicationEvent);
                    }
                }
            }
        }
        return totalCount;
    }

}
