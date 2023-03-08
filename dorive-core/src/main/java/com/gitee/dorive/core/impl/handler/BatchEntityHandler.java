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

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.EntityIndex;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;

import java.util.List;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final OperationFactory operationFactory;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository, OperationFactory operationFactory) {
        this.repository = repository;
        this.operationFactory = operationFactory;
    }

    @Override
    public int handleEntities(Context context, List<Object> rootEntities) {
        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            if (selector.matches(context, repository)) {
                totalCount += executeQuery(repository, context, rootEntities);
            }
        }
        return totalCount;
    }

    private int executeQuery(CommonRepository repository, Context context, List<Object> rootEntities) {
        UnionExample unionExample = newUnionExample(repository, context, rootEntities);
        if (unionExample.isDirtyQuery()) {
            Query query = operationFactory.buildQuery(context, unionExample);
            query.setType(query.getType() | Operation.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof EntityIndex) {
                setValueForRootEntities(repository, rootEntities, (EntityIndex) result);
            }
            return result.getTotal();
        }
        return 0;
    }

    private UnionExample newUnionExample(CommonRepository repository, Context context, List<Object> rootEntities) {
        PropChain anchorPoint = repository.getAnchorPoint();
        PropChain lastPropChain = anchorPoint.getLastPropChain();

//        Map<String, Object> attachments = context.getAttachments();
//        String builderKey = repository.getEntityDef().getBuilderKey();
//        ExampleBuilder exampleBuilder = StringUtils.isNotBlank(builderKey) ? (ExampleBuilder) attachments.get(builderKey) : null;

        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            Object lastEntity = lastPropChain == null ? rootEntity : lastPropChain.getValue(rootEntity);
            if (lastEntity != null) {
                Example example = repository.newExampleByContext(context, rootEntity);
//                if (exampleBuilder != null) {
//                    example = exampleBuilder.buildExample(context, rootEntity, example);
//                }
                if (example.isDirtyQuery()) {
                    example.extraColumns((index + 1) + " as $row");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    private void setValueForRootEntities(CommonRepository repository, List<Object> rootEntities, EntityIndex entityIndex) {
        PropChain anchorPoint = repository.getAnchorPoint();
        PropChain lastPropChain = anchorPoint.getLastPropChain();
        PropProxy propProxy = anchorPoint.getPropProxy();

        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            Object lastEntity = lastPropChain == null ? rootEntity : lastPropChain.getValue(rootEntity);
            if (lastEntity != null) {
                List<Object> entities = entityIndex.selectList(rootEntity, index + 1);
                Object entity = repository.convertManyToOne(entities);
                if (entity != null) {
                    propProxy.setValue(lastEntity, entity);
                }
            }
        }
    }

}
