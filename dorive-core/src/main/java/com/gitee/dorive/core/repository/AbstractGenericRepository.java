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
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.api.ListableRepository;
import com.gitee.dorive.core.api.MetadataHolder;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.operation.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK>
        implements ListableRepository<E, PK>, MetadataHolder {

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (ConfiguredRepository repository : getOrderedRepositories()) {
            if (boundedContext.isMatch(repository)) {
                totalCount += repository.updateByExample(boundedContext, entity, example);
            }
        }
        return totalCount;
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        Operation operation = new Operation(Operation.INSERT_OR_UPDATE, entity);
        return execute(boundedContext, operation);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Assert.notNull(primaryKey, "The primaryKey cannot be null!");
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(boundedContext, entity);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example, "The example cannot be null!");
        int totalCount = 0;
        for (ConfiguredRepository repository : getOrderedRepositories()) {
            if (boundedContext.isMatch(repository)) {
                totalCount += repository.deleteByExample(boundedContext, example);
            }
        }
        return totalCount;
    }

    @Override
    public int insertList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insert(boundedContext, entity)).sum();
    }

    @Override
    public int updateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> update(boundedContext, entity)).sum();
    }

    @Override
    public int insertOrUpdateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insertOrUpdate(boundedContext, entity)).sum();
    }

    @Override
    public int deleteList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(boundedContext, entity)).sum();
    }

    @Override
    public Object getMetadata() {
        return rootRepository.getMetadata();
    }

}
