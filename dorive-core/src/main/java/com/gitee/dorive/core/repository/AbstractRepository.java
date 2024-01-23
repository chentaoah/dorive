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
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.repository.Repository;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class AbstractRepository<E, PK> implements Repository<E, PK>, Executor {

    private EntityDef entityDef;
    private EntityEle entityEle;
    private OperationFactory operationFactory;
    private Executor executor;

    public Class<?> getEntityClass() {
        return entityEle.getGenericType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(Context context, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        Query query = operationFactory.buildQueryByPK(primaryKey);
        Result<Object> result = executeQuery(context, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        Result<Object> result = executeQuery(context, query);
        return (List<E>) result.getRecords();
    }

    @Override
    public E selectOneByExample(Context context, Example example) {
        List<E> entities = selectByExample(context, example);
        return entities != null && !entities.isEmpty() ? entities.get(0) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        Result<Object> result = executeQuery(context, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public long selectCountByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQueryByExample(example);
        return executeCount(context, query);
    }

    @Override
    public int insert(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Insert insert = operationFactory.buildInsert(entity);
        return execute(context, insert);
    }

    @Override
    public int update(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Update update = operationFactory.buildUpdate(entity);
        return execute(context, update);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Update update = operationFactory.buildUpdateByExample(entity, example);
        return execute(context, update);
    }

    @Override
    public int insertOrUpdate(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildInsertOrUpdate(entity);
        return execute(context, operation);
    }

    @Override
    public int delete(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Delete delete = operationFactory.buildDelete(entity);
        return execute(context, delete);
    }

    @Override
    public int deleteByPrimaryKey(Context context, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        Delete delete = operationFactory.buildDeleteByPK(primaryKey);
        return execute(context, delete);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Delete delete = operationFactory.buildDeleteByExample(example);
        return execute(context, delete);
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
