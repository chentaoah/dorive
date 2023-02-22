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
package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.EntityIndex;
import com.gitee.dorive.core.api.ExampleBuilder;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.element.PropertyChain;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.OperationFactory;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final OperationFactory operationFactory;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository, OperationFactory operationFactory) {
        this.repository = repository;
        this.operationFactory = operationFactory;
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            if (boundedContext.isMatch(repository)) {
                executeQuery(repository, boundedContext, rootEntities);
            }
        }
    }

    private void executeQuery(CommonRepository repository, BoundedContext boundedContext, List<Object> rootEntities) {
        UnionExample unionExample = newUnionExample(repository, boundedContext, rootEntities);
        if (unionExample.isDirtyQuery()) {
            Query query = operationFactory.buildQuery(boundedContext, unionExample);
            query.setType(query.getType() | Operation.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(boundedContext, query);
            if (result instanceof EntityIndex) {
                setValueForRootEntities(repository, rootEntities, (EntityIndex) result);
            }
        }
    }

    private UnionExample newUnionExample(CommonRepository repository, BoundedContext boundedContext, List<Object> rootEntities) {
        PropertyChain anchorPoint = repository.getAnchorPoint();
        PropertyChain lastPropertyChain = anchorPoint.getLastPropertyChain();

        String builderKey = repository.getEntityDefinition().getBuilderKey();
        ExampleBuilder exampleBuilder = StringUtils.isNotBlank(builderKey) ? (ExampleBuilder) boundedContext.get(builderKey) : null;

        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                Example example = repository.newExampleByContext(boundedContext, rootEntity);
                if (exampleBuilder != null) {
                    example = exampleBuilder.buildExample(boundedContext, rootEntity, example);
                }
                if (example.isDirtyQuery()) {
                    example.extraColumns((index + 1) + " as $row");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    private void setValueForRootEntities(CommonRepository repository, List<Object> rootEntities, EntityIndex entityIndex) {
        PropertyChain anchorPoint = repository.getAnchorPoint();
        PropertyChain lastPropertyChain = anchorPoint.getLastPropertyChain();
        PropertyProxy propertyProxy = anchorPoint.getPropertyProxy();

        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                List<Object> entities = entityIndex.selectList(rootEntity, index + 1);
                Object entity = repository.convertManyToOne(entities);
                if (entity != null) {
                    propertyProxy.setValue(lastEntity, entity);
                }
            }
        }
    }

}
