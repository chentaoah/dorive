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

package com.gitee.dorive.core.impl.executor;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.EntityOpHandler;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.impl.repository.AbstractContextRepository;
import com.gitee.dorive.core.impl.repository.ProxyRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContextExecutor extends AbstractExecutor implements EntityHandler, EntityOpHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;
    private final EntityOpHandler entityOpHandler;

    public ContextExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler, EntityOpHandler entityOpHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
        this.entityOpHandler = entityOpHandler;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Assert.isTrue(!query.isEmpty(), "The query cannot be empty!");
        ProxyRepository rootRepository = repository.getRootRepository();
        if (rootRepository.matches(context) || query.isIncludeRoot()) {
            Result<Object> result = rootRepository.executeQuery(context, query);
            List<Object> entities = result.getRecords();
            if (!entities.isEmpty()) {
                handle(context, entities);
            }
            return result;
        }
        return Result.emptyResult(query);
    }

    @Override
    public long handle(Context context, List<Object> entities) {
        return entityHandler.handle(context, entities);
    }

    @Override
    public long executeCount(Context context, Query query) {
        throw new RuntimeException("This method does not support!");
    }

    @Override
    public int execute(Context context, Operation operation) {
        EntityOp entityOp = (EntityOp) operation;
        List<?> entities = entityOp.getEntities();
        Assert.notEmpty(entities, "The entities cannot be empty!");
        return (int) handle(context, entityOp);
    }

    @Override
    public long handle(Context context, EntityOp entityOp) {
        return entityOpHandler.handle(context, entityOp);
    }

}
