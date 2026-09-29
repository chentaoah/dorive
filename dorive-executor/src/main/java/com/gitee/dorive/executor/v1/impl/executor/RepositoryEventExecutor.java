/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.executor;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.event.api.EventPublisher;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.Insert;
import com.gitee.dorive.base.v1.executor.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.executor.entity.eop.Update;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryEventExecutor extends AbstractProxyExecutor {

    private final RepositoryContext repositoryContext;

    public RepositoryEventExecutor(RepositoryContext repositoryContext, Executor executor) {
        super(executor);
        this.repositoryContext = repositoryContext;
    }

    @Override
    public int execute(Context context, Operation operation) {
        int totalCount = super.execute(context, operation);
        if (totalCount != 0) {
            if (operation instanceof InsertOrUpdate insertOrUpdate) {
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
        if (operation instanceof EntityOp entityOp) {
            EntityElement entityElement = repositoryContext.getEntityElement();
            EventPublisher eventPublisher = repositoryContext.getRepositoryEventPublisher();
            eventPublisher.publishEvent(entityElement.getGenericType(), context, entityOp);
        }
    }
}
