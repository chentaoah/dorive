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

package com.gitee.dorive.repository.v1.impl.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.core.util.ExampleUtils;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.repository.v1.api.GenericRepository;

import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractRepositoryContext implements GenericRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E selectOneByPrimaryKey(Options options, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        Query query = getOperationFactory().buildQueryByPK(primaryKey);
        Result<Object> result = executeQuery((Context) options, query);
        return (E) result.getRecord();
    }

    @Override
    public E selectOneByExample(Options options, Example example) {
        List<E> entities = selectByExample(options, example);
        return entities != null && !entities.isEmpty() ? entities.get(0) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Query query = getOperationFactory().buildQueryByExample(example);
        Result<Object> result = executeQuery((Context) options, query);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = getOperationFactory().buildQueryByExample(example);
        Result<Object> result = executeQuery((Context) options, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public long selectCountByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        return getRootRepository().selectCountByExample(options, example);
    }

    @Override
    public int insert(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = getOperationFactory().buildInsert(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int update(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = getOperationFactory().buildUpdate(options, entity);
        return execute((Context) options, operation);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (RepositoryItem repositoryItem : getOrderedRepositories()) {
            if (matches(options, repositoryItem)) {
                totalCount += repositoryItem.updateByExample(options, entity, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

    @Override
    public int insertOrUpdate(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = getOperationFactory().buildInsertOrUpdate(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int delete(Options options, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = getOperationFactory().buildDelete(entity);
        return execute((Context) options, operation);
    }

    @Override
    public int deleteByPrimaryKey(Options options, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        E entity = selectOneByPrimaryKey(options, primaryKey);
        return delete(options, entity);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (RepositoryItem repositoryItem : getOrderedRepositories()) {
            if (matches(options, repositoryItem)) {
                totalCount += repositoryItem.deleteByExample(options, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

    // ================================================================================

    @Override
    public int insertList(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        if (entities.isEmpty()) {
            return 0;
        }
        Operation operation = new Insert(entities);
        return execute((Context) options, operation);
    }

    @Override
    public int updateList(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        if (entities.isEmpty()) {
            return 0;
        }
        Operation operation = new Update(entities);
        return execute((Context) options, operation);
    }

    @Override
    public int insertOrUpdateList(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        if (entities.isEmpty()) {
            return 0;
        }
        OperationFactory operationFactory = getOperationFactory();
        Operation operation = operationFactory.buildInsertOrUpdate(entities);
        return execute((Context) options, operation);
    }

    @Override
    public int deleteList(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        if (entities.isEmpty()) {
            return 0;
        }
        Operation operation = new Delete(entities);
        return execute((Context) options, operation);
    }

    // ================================================================================

    @Override
    public E findOneById(PK id) {
        return selectOneByPrimaryKey(Options.ROOT, id);
    }

    @Override
    public E findOne(Example example) {
        return selectOneByExample(Options.ROOT, example);
    }

    @Override
    public List<E> find(Example example) {
        return selectByExample(Options.ROOT, example);
    }

    @Override
    public List<E> findAll() {
        return selectByExample(Options.ROOT, new InnerExample());
    }

    @Override
    public long count(Example example) {
        return selectCountByExample(Options.ROOT, example);
    }

    @Override
    public boolean exist(Example example) {
        return selectCountByExample(Options.ROOT, example) > 0;
    }

    @Override
    public boolean save(E entity) {
        return insert(Options.ROOT, entity) > 0;
    }

    @Override
    public boolean save(List<E> entities) {
        return insertList(Options.ROOT, entities) > 0;
    }

    @Override
    public boolean deleteById(PK id) {
        return deleteByPrimaryKey(Options.ROOT, id) > 0;
    }

    @Override
    public boolean delete(E entity) {
        return delete(Options.ROOT, entity) > 0;
    }

    @Override
    public boolean delete(List<E> entities) {
        return deleteList(Options.ROOT, entities) > 0;
    }

}
