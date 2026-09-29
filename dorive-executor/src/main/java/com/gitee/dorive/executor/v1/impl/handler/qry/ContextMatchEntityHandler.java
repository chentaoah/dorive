/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ContextMatchEntityHandler implements EntityHandler {

    private RepositoryContext repositoryContext;
    private RepositoryItem repositoryItem;
    private EntityHandler entityHandler;

    @Override
    public long handle(Context context, List<Object> entities) {
        return repositoryContext.matches(context, null, repositoryItem) ? entityHandler.handle(context, entities) : 0L;
    }

}
