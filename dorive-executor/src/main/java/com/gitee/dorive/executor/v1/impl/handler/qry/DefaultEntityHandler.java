/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.binder.api.ExampleBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DefaultEntityHandler implements EntityHandler {

    private final RepositoryItem repositoryItem;
    private final ExampleBuilder exampleBuilder;
    private final EntityJoiner entityJoiner;

    @Override
    public long handle(Context context, List<Object> entities) {
        OperationFactory operationFactory = repositoryItem.getOperationFactory();
        BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();

        Example example = exampleBuilder.newExample(context, entities);
        binderExecutor.appendFilterCriteria(context, example);
        if (example.isEmpty()) {
            return 0L;
        }
        Query query = operationFactory.buildQueryByExample(example);
        query.setMatched(true);
        Result<Object> result = repositoryItem.executeQuery(context, query);
        entityJoiner.join(context, entities, result.getRecords());
        return result.getCount();
    }

}
