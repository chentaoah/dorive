/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.executor;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.event.api.EventPublisher;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.Getter;
import lombok.Setter;

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
            publishEvent(context, operation);
        }
        return totalCount;
    }

    private void publishEvent(Context context, Operation operation) {
        if (operation instanceof EntityOp entityOp) {
            EntityElement entityElement = getEntityElement();
            EventPublisher eventPublisher = repositoryContext.getExecutorEventPublisher();
            eventPublisher.publishEvent(entityElement.getGenericType(), context, entityOp);
        }
    }
}
