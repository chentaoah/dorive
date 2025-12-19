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

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;

public interface RepositoryBuilder {

    AbstractRepository<Object, Object> newRepository(RepositoryContext repositoryContext, EntityElement entityElement);

    BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement);

    void buildRepositoryItem(RepositoryItem repositoryItem);

    Executor newExecutor(RepositoryContext repositoryContext);

    EntityHandler newEntityHandler(RepositoryContext repositoryContext);

    EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext);

    void buildQueryRepository(RepositoryContext repositoryContext);

    void buildQueryRepository2(RepositoryContext repositoryContext);

    void buildMybatisRepository(RepositoryContext repositoryContext);

}
