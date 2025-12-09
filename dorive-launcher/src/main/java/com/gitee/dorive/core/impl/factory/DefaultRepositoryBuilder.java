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

package com.gitee.dorive.core.impl.factory;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import com.gitee.dorive.executor.v1.impl.context.AdaptiveMatcher;
import com.gitee.dorive.executor.v1.impl.handler.op.BatchEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.BatchEntityHandler;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;

public class DefaultRepositoryBuilder implements RepositoryBuilder {

    @Override
    public BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement) {
        BinderResolver binderResolver = new BinderResolver(repositoryContext);
        binderResolver.resolve(entityElement);
        return binderResolver;
    }

    @Override
    public Matcher newAdaptiveMatcher(boolean root, String name) {
        return new AdaptiveMatcher(root, name);
    }

    @Override
    public EntityHandler newEntityHandler(RepositoryContext repositoryContext) {
        return new BatchEntityHandler(repositoryContext);
    }

    @Override
    public EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext) {
        return new BatchEntityOpHandler(repositoryContext);
    }

}
