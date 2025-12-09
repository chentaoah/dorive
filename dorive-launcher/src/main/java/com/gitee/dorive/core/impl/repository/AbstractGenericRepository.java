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

package com.gitee.dorive.core.impl.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.repository.v1.api.ListableRepository;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.util.ExampleUtils;

import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK> implements ListableRepository<E, PK> {

    @Override
    public long selectCountByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        ProxyRepository repository = getRootRepository();
        return repository.selectCountByExample(options, example);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (ProxyRepository repository : getOrderedRepositories()) {
            if (repository.matches(options)) {
                totalCount += repository.updateByExample(options, entity, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

    @Override
    public int deleteByPrimaryKey(Options options, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        E entity = selectByPrimaryKey(options, primaryKey);
        return delete(options, entity);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (ProxyRepository repository : getOrderedRepositories()) {
            if (repository.matches(options)) {
                totalCount += repository.deleteByExample(options, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

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

}
