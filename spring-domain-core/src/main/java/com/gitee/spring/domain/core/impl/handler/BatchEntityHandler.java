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
package com.gitee.spring.domain.core.impl.handler;

import com.gitee.spring.domain.core.api.EntityHandler;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.ExampleBuilder;
import com.gitee.spring.domain.core.api.PropertyProxy;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Result;
import com.gitee.spring.domain.core.entity.executor.UnionExample;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        for (ConfiguredRepository repository : this.repository.getSubRepositories()) {
            if (repository.matchKeys(boundedContext)) {
                UnionExample unionExample = newUnionExample(repository, boundedContext, rootEntities);
                if (!unionExample.isDirtyQuery()) {
                    continue;
                }
                Result<Object> result = repository.selectResultByExample(boundedContext, unionExample);
                if (!(result instanceof EntityIndex)) {
                    continue;
                }
                setValueForRootEntities(repository, rootEntities, (EntityIndex) result);
            }
        }
    }

    private UnionExample newUnionExample(ConfiguredRepository repository, BoundedContext boundedContext, List<Object> rootEntities) {
        ConfiguredRepository rootRepository = this.repository.getRootRepository();

        PropertyChain anchorPoint = repository.getAnchorPoint();
        PropertyChain lastPropertyChain = anchorPoint.getLastPropertyChain();

        String builderKey = repository.getEntityDefinition().getBuilderKey();
        ExampleBuilder exampleBuilder = StringUtils.isNotBlank(builderKey) ? (ExampleBuilder) boundedContext.get(builderKey) : null;

        UnionExample unionExample = new UnionExample();
        for (Object rootEntity : rootEntities) {
            Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                Example example = repository.newExampleByContext(boundedContext, rootEntity);
                if (exampleBuilder != null) {
                    example = exampleBuilder.buildExample(boundedContext, rootEntity, example);
                }
                if (example.isDirtyQuery()) {
                    Object primaryKey = rootRepository.getPrimaryKey(rootEntity);
                    example.selectColumns(primaryKey + " as $id");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    private void setValueForRootEntities(ConfiguredRepository repository, List<Object> rootEntities, EntityIndex entityIndex) {
        ConfiguredRepository rootRepository = this.repository.getRootRepository();

        PropertyChain anchorPoint = repository.getAnchorPoint();
        PropertyChain lastPropertyChain = anchorPoint.getLastPropertyChain();
        PropertyProxy propertyProxy = anchorPoint.getPropertyProxy();

        for (Object rootEntity : rootEntities) {
            Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                Object primaryKey = rootRepository.getPrimaryKey(rootEntity);
                List<Object> entities = entityIndex.selectList(rootEntity, primaryKey);
                Object entity = repository.convertManyToOne(entities);
                if (entity != null) {
                    propertyProxy.setValue(lastEntity, entity);
                }
            }
        }
    }

}
