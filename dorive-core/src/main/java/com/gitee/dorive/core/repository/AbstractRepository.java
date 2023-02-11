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
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.api.Repository;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.element.EntityElement;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.*;
import com.gitee.dorive.core.impl.OperationFactory;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractRepository<E, PK> implements Repository<E, PK>, Executor {
    
    protected EntityDefinition entityDefinition;
    protected EntityElement entityElement;
    protected OperationFactory operationFactory;
    protected Executor executor;

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Query query = operationFactory.buildQueryByPK(boundedContext, primaryKey);
        Result<Object> result = executeQuery(boundedContext, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQuery(boundedContext, example);
        Result<Object> result = executeQuery(boundedContext, query);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = operationFactory.buildQuery(boundedContext, example);
        Result<Object> result = executeQuery(boundedContext, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Insert insert = operationFactory.buildInsert(boundedContext, entity);
        return execute(boundedContext, insert);
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Update update = operationFactory.buildUpdate(boundedContext, entity);
        return execute(boundedContext, update);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Update update = operationFactory.buildUpdate(boundedContext, entity, example);
        return execute(boundedContext, update);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildInsertOrUpdate(boundedContext, entity);
        return execute(boundedContext, operation);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Delete delete = operationFactory.buildDelete(boundedContext, entity);
        return execute(boundedContext, delete);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Delete delete = operationFactory.buildDeleteByPK(boundedContext, primaryKey);
        return execute(boundedContext, delete);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Delete delete = operationFactory.buildDelete(boundedContext, example);
        return execute(boundedContext, delete);
    }

    @Override
    public Result<Object> executeQuery(BoundedContext boundedContext, Query query) {
        return executor.executeQuery(boundedContext, query);
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        return executor.execute(boundedContext, operation);
    }

}
