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

package com.gitee.dorive.base.v1.repository.api;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;

import java.util.Collection;

public interface RepositoryItem extends Repository<Object, Object>, Properties {

    EntityElement getEntityElement();

    OperationFactory getOperationFactory();

    AbstractRepository<Object, Object> getProxyRepository();

    boolean isCollection();

    Class<?> getEntityClass();

    boolean hasField(String field);

    String getAccessPath();

    boolean isRoot();

    boolean isAggregated();

    BinderExecutor getBinderExecutor();

    void getBoundValue(Context context, Object rootEntity, Collection<?> entities);

    void setBoundId(Context context, Object rootEntity, Object entity);

    void setBound(boolean bound);

    boolean isBound();

    boolean matches(Options options);

    Result<Object> executeQuery(Context context, Query query);

    int execute(Context context, Operation operation);

}
