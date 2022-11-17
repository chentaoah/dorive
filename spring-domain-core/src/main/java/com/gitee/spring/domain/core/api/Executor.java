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
package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.*;
import com.gitee.spring.domain.core.entity.operation.Delete;
import com.gitee.spring.domain.core.entity.operation.Insert;
import com.gitee.spring.domain.core.entity.operation.Operation;
import com.gitee.spring.domain.core.entity.operation.Query;
import com.gitee.spring.domain.core.entity.operation.Update;

public interface Executor {

    Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey);

    Query buildQuery(BoundedContext boundedContext, Example example);

    Result<Object> executeQuery(BoundedContext boundedContext, Query query);

    Insert buildInsert(BoundedContext boundedContext, Object entity);

    Update buildUpdate(BoundedContext boundedContext, Object entity);

    Update buildUpdate(BoundedContext boundedContext, Object entity, Example example);

    Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity);

    Delete buildDelete(BoundedContext boundedContext, Object entity);

    Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey);

    Delete buildDelete(BoundedContext boundedContext, Example example);

    int execute(BoundedContext boundedContext, Operation operation);

}
