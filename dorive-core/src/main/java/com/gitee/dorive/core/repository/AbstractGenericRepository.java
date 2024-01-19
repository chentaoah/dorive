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
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.repository.ListableRepository;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.InsertOrUpdate;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.util.ExampleUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK> implements ListableRepository<E, PK> {

    @Override
    public long selectCountByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        CommonRepository repository = getRootRepository();
        return repository.selectCountByExample(context, example);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : getOrderedRepositories()) {
            if (selector.matches(context, repository)) {
                totalCount += repository.updateByExample(context, entity, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

    @Override
    public int insertOrUpdate(Context context, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = new InsertOrUpdate(entity);
        return execute(context, operation);
    }

    @Override
    public int deleteByPrimaryKey(Context context, PK primaryKey) {
        Assert.notNull(primaryKey, "The primary key cannot be null!");
        E entity = selectByPrimaryKey(context, primaryKey);
        return delete(context, entity);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : getOrderedRepositories()) {
            if (selector.matches(context, repository)) {
                totalCount += repository.deleteByExample(context, ExampleUtils.clone(example));
            }
        }
        return totalCount;
    }

    @Override
    public int insertList(Context context, List<E> entities) {
        return entities.stream().mapToInt(entity -> insert(context, entity)).sum();
    }

    @Override
    public int updateList(Context context, List<E> entities) {
        return entities.stream().mapToInt(entity -> update(context, entity)).sum();
    }

    @Override
    public int insertOrUpdateList(Context context, List<E> entities) {
        return entities.stream().mapToInt(entity -> insertOrUpdate(context, entity)).sum();
    }

    @Override
    public int deleteList(Context context, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(context, entity)).sum();
    }

}
