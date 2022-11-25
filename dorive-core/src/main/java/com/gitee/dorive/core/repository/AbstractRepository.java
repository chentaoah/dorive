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
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.api.Repository;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.entity.executor.*;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractRepository<E, PK> implements Repository<E, PK> {

    protected EntityElement entityElement;
    protected EntityDefinition entityDefinition;
    protected Executor executor;

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Query query = executor.buildQueryByPK(boundedContext, primaryKey);
        Result<Object> result = executor.executeQuery(boundedContext, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = executor.buildQuery(boundedContext, example);
        Result<Object> result = executor.executeQuery(boundedContext, query);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = executor.buildQuery(boundedContext, example);
        Result<Object> result = executor.executeQuery(boundedContext, query);
        return (Page<E>) result.getPage();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<E> selectResultByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = executor.buildQuery(boundedContext, example);
        return (Result<E>) executor.executeQuery(boundedContext, query);
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Insert insert = executor.buildInsert(boundedContext, entity);
        return executor.execute(boundedContext, insert);
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Update update = executor.buildUpdate(boundedContext, entity);
        return executor.execute(boundedContext, update);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Update update = executor.buildUpdate(boundedContext, entity, example);
        return executor.execute(boundedContext, update);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = executor.buildInsertOrUpdate(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Delete delete = executor.buildDelete(boundedContext, entity);
        return executor.execute(boundedContext, delete);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Delete delete = executor.buildDeleteByPK(boundedContext, primaryKey);
        return executor.execute(boundedContext, delete);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Delete delete = executor.buildDelete(boundedContext, example);
        return executor.execute(boundedContext, delete);
    }

}
