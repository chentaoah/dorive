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
import com.gitee.dorive.core.api.Adapter;
import com.gitee.dorive.core.api.Context;
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
        this.repository = repository;
        this.executor = executor;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        AdapterResolver adapterResolver = repository.getAdapterResolver();
        Adapter adapter = adapterResolver.getAdapter();
        adapter.adapt(context, query);
        return executor.executeQuery(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        AdapterResolver adapterResolver = repository.getAdapterResolver();
        Adapter adapter = adapterResolver.getAdapter();
        adapter.adapt(context, operation);
        return executor.execute(context, operation);
    }

}
