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
package com.gitee.spring.domain.core.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.*;
import com.gitee.spring.domain.core.entity.operation.Delete;
import com.gitee.spring.domain.core.entity.operation.Insert;
import com.gitee.spring.domain.core.entity.operation.Operation;
import com.gitee.spring.domain.core.entity.operation.Query;
import com.gitee.spring.domain.core.entity.operation.Update;

public abstract class AbstractExecutor implements Executor {

    @Override
    public Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey) {
        Query query = new Query(Operation.SELECT, null);
        query.setPrimaryKey(primaryKey);
        return query;
    }

    @Override
    public Query buildQuery(BoundedContext boundedContext, Example example) {
        Query query = new Query(Operation.SELECT, null);
        query.setExample(example);
        return query;
    }

    @Override
    public Insert buildInsert(BoundedContext boundedContext, Object entity) {
        return new Insert(Operation.INSERT, entity);
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setPrimaryKey(BeanUtil.getFieldValue(entity, "id"));
        return update;
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setExample(example);
        return update;
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            return new Insert(Operation.INSERT, entity);
        } else {
            Update update = new Update(Operation.UPDATE, entity);
            update.setPrimaryKey(primaryKey);
            return update;
        }
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Object entity) {
        Delete delete = new Delete(Operation.DELETE, entity);
        delete.setPrimaryKey(BeanUtil.getFieldValue(entity, "id"));
        return delete;
    }

    @Override
    public Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Example example) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setExample(example);
        return delete;
    }

}
