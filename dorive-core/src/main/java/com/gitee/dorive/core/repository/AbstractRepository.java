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
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.*;
import com.gitee.dorive.core.impl.OperationFactory;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractRepository<E, PK> implements Repository<E, PK>, Executor {

    protected EntityEle entityEle;
    protected OperationFactory operationFactory;
    protected Executor executor;

    public EntityDef getEntityDef() {
        return entityEle.getEntityDef();
    }

    public Class<?> getEntityClass() {
        return entityEle.getGenericType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(Context context, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Query query = operationFactory.buildQueryByPK(context, primaryKey);
        Result<Object> result = executeQuery(context, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = operationFactory.buildQuery(context, example);
        Result<Object> result = executeQuery(context, query);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = operationFactory.buildQuery(context, example);
        Result<Object> result = executeQuery(context, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public int insert(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Insert insert = operationFactory.buildInsert(context, entity);
        return execute(context, insert);
    }

    @Override
    public int update(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Update update = operationFactory.buildUpdate(context, entity);
        return execute(context, update);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Update update = operationFactory.buildUpdate(context, entity, example);
        return execute(context, update);
    }

    @Override
    public int insertOrUpdate(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = operationFactory.buildInsertOrUpdate(context, entity);
        return execute(context, operation);
    }

    @Override
    public int delete(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Delete delete = operationFactory.buildDelete(context, entity);
        return execute(context, delete);
    }

    @Override
    public int deleteByPrimaryKey(Context context, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        Delete delete = operationFactory.buildDeleteByPK(context, primaryKey);
        return execute(context, delete);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Delete delete = operationFactory.buildDelete(context, example);
        return execute(context, delete);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        return executor.executeQuery(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        return executor.execute(context, operation);
    }

}
