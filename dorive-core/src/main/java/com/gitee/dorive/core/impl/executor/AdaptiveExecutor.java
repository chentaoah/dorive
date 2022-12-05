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

import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.api.ContextAdapter;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.resolver.AdapterResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdaptiveExecutor extends AbstractExecutor {

    private AbstractContextRepository<?, ?> repository;
    private Executor executor;

    public AdaptiveExecutor(AbstractContextRepository<?, ?> repository, Executor executor) {
        super(repository.getEntityElement());
        this.repository = repository;
        this.executor = executor;
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        return new Operation(Operation.INSERT_OR_UPDATE, entity);
    }

    @Override
    public Result<Object> executeQuery(BoundedContext boundedContext, Query query) {
        AdapterResolver adapterResolver = repository.getAdapterResolver();
        ContextAdapter contextAdapter = adapterResolver.getContextAdapter();
        contextAdapter.adapt(boundedContext, query);
        return executor.executeQuery(boundedContext, query);
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        AdapterResolver adapterResolver = repository.getAdapterResolver();
        ContextAdapter contextAdapter = adapterResolver.getContextAdapter();
        contextAdapter.adapt(boundedContext, operation);
        return executor.execute(boundedContext, operation);
    }

}
