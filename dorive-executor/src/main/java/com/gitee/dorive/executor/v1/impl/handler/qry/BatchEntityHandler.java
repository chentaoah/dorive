/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchEntityHandler implements EntityHandler {

    private final RepositoryContext repositoryContext;
    private final List<EntityHandler> entityHandlers;

    @Override
    public long handle(Context context, List<Object> entities) {
        long totalCount = 0L;
        for (EntityHandler entityHandler : entityHandlers) {
            totalCount += entityHandler.handle(context, entities);
        }
        return totalCount;
    }

}
