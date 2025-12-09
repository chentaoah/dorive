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

package com.gitee.dorive.event.impl.repository;

import cn.hutool.core.util.ArrayUtil;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.core.entity.op.EntityOp;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.core.impl.repository.AbstractGenericRepository;
import com.gitee.dorive.core.impl.repository.AbstractRepository;
import com.gitee.dorive.core.impl.repository.DefaultRepository;
import com.gitee.dorive.event.entity.BaseEvent;
import com.gitee.dorive.event.entity.ExecutorEvent;
import com.gitee.dorive.event.entity.RepositoryEvent;
import com.gitee.dorive.event.impl.executor.EventExecutor;
import com.gitee.dorive.event.impl.factory.EventFactory;

public abstract class AbstractEventRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    private boolean enableExecutorEvent;
    private boolean enableRepositoryEvent;

    @Override
    protected void prepareRepositoryDef(Class<?> repositoryClass, Class<?> entityClass) {
        super.prepareRepositoryDef(repositoryClass, entityClass);
        RepositoryDef repositoryDef = getRepositoryDef();
        Class<?>[] events = repositoryDef.getEvents();
        enableExecutorEvent = ArrayUtil.contains(events, ExecutorEvent.class);
        enableRepositoryEvent = ArrayUtil.contains(events, RepositoryEvent.class);
    }

    @Override
    protected AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository) {
        if (enableExecutorEvent && repository instanceof DefaultRepository) {
            Executor executor = repository.getExecutor();
            executor = new EventExecutor(executor, getApplicationContext(), repository.getEntityElement());
            repository.setExecutor(executor);
        }
        return repository;
    }

    @Override
    public int execute(Context context, Operation operation) {
        int totalCount = super.execute(context, operation);
        if (enableRepositoryEvent && totalCount != 0) {
            if (operation instanceof InsertOrUpdate) {
                InsertOrUpdate insertOrUpdate = (InsertOrUpdate) operation;
                Insert insert = insertOrUpdate.getInsert();
                Update update = insertOrUpdate.getUpdate();
                if (insert != null) {
                    publishEvent(context, insert);
                }
                if (update != null) {
                    publishEvent(context, update);
                }
            } else {
                publishEvent(context, operation);
            }
        }
        return totalCount;
    }

    private void publishEvent(Context context, Operation operation) {
        if (operation instanceof EntityOp) {
            Class<?> entityClass = getEntityElement().getGenericType();
            EntityOp entityOp = (EntityOp) operation;
            BaseEvent<?> baseEvent = EventFactory.newRepositoryEvent(this, entityOp.isUncontrolled(), entityClass, context, entityOp);
            getApplicationContext().publishEvent(baseEvent);
        }
    }

}
