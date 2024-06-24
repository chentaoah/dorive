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

package com.gitee.dorive.core.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.def.EntityDef;
import com.gitee.dorive.api.def.OrderDef;
import com.gitee.dorive.api.ele.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.repository.Repository;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class AbstractRepository<E, PK> implements Repository<E, PK>, Executor {

    private EntityDef entityDef;
    private OrderDef orderDef;
    private EntityElement entityElement;
    private OperationFactory operationFactory;
    private Executor executor;

    public Class<?> getEntityClass() {
        return entityElement.getGenericType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(Options options, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        Query query = operationFactory.buildQueryByPK(primaryKey);
        Result<Object> result = executeQuery((Context) options, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        Result<Object> result = executeQuery((Context) options, query);
        return (List<E>) result.getRecords();
    }

    @Override
    public E selectOneByExample(Options options, Example example) {
        List<E> entities = selectByExample(options, example);
        return entities != null && !entities.isEmpty() ? entities.get(0) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        Result<Object> result = executeQuery((Context) options, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public long selectCountByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        return executeCount((Context) options, query);
    }

    @Override
    public int insert(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildInsert(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int update(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildUpdate(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Operation operation = operationFactory.buildUpdateByExample(entity, example);
        return execute((Context) options, operation);
    }

    @Override
    public int insertOrUpdate(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildInsertOrUpdate(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int delete(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildDelete(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int deleteByPrimaryKey(Options options, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        Operation operation = operationFactory.buildDeleteByPK(primaryKey);
        return execute((Context) options, operation);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Operation operation = operationFactory.buildDeleteByExample(example);
        return execute((Context) options, operation);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        return executor.executeQuery(context, query);
    }

    @Override
    public long executeCount(Context context, Query query) {
        return executor.executeCount(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        return executor.execute(context, operation);
    }

}
