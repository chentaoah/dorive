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

import com.gitee.dorive.api.constant.OperationType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final OperationFactory operationFactory;
    private final MultiQuerier multiQuerier;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository, OperationFactory operationFactory) {
        this.repository = repository;
        this.operationFactory = operationFactory;
        this.multiQuerier = new MultiQuerier(operationFactory);
    }

    @Override
    public int handle(Context context, List<Object> entities) {
        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            if (selector.matches(context, repository)) {
                if (isMultiQuery(entities, repository)) {
                    totalCount += multiQuerier.executeQuery(context, entities, repository);
                } else {
                    totalCount += executeQuery(context, entities, repository);
                }
            }
        }
        return totalCount;
    }

    private boolean isMultiQuery(List<Object> rootEntities, CommonRepository repository) {
        if (rootEntities.size() > 10) {
            BinderResolver binderResolver = repository.getBinderResolver();
            Map<String, List<PropertyBinder>> mergedBindersMap = binderResolver.getMergedBindersMap();
            return mergedBindersMap.size() == 1 && mergedBindersMap.containsKey("/");
        }
        return false;
    }

    private long executeQuery(Context context, List<Object> rootEntities, CommonRepository repository) {
        UnionExample unionExample = newUnionExample(context, rootEntities, repository);
        if (unionExample.isDirtyQuery()) {
            Query query = operationFactory.buildQuery(unionExample);
            query.setType(query.getType() | OperationType.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(rootEntities, repository, (MultiResult) result);
            }
            return result.getCount();
        }
        return 0;
    }

    private UnionExample newUnionExample(Context context, List<Object> rootEntities, CommonRepository repository) {
        PropChain anchorPoint = repository.getAnchorPoint();
        PropChain lastPropChain = anchorPoint.getLastPropChain();
        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            Object lastEntity = lastPropChain == null ? rootEntity : lastPropChain.getValue(rootEntity);
            if (lastEntity != null) {
                Example example = repository.newExampleByContext(context, rootEntity);
                if (example.isDirtyQuery()) {
                    example.selectExtra((index + 1) + " as $row");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    private void setValueForRootEntities(List<Object> rootEntities, CommonRepository repository, MultiResult multiResult) {
        boolean isCollection = repository.getEntityEle().isCollection();
        PropChain anchorPoint = repository.getAnchorPoint();

        List<Map<String, Object>> resultMaps = multiResult.getResultMaps();
        List<Object> entities = multiResult.getRecords();
        int averageSize = entities.size() / rootEntities.size() + 1;
        int lastRowNum = -1;
        Collection<Object> lastCollection = null;

        for (int index = 0; index < entities.size(); index++) {
            Map<String, Object> resultMap = resultMaps.get(index);
            Object entity = entities.get(index);
            Integer rowNum = NumberUtils.intValue(resultMap.get("$row"));

            if (rowNum == lastRowNum) {
                if (isCollection && lastCollection != null) {
                    lastCollection.add(entity);
                }

            } else {
                Object rootEntity = rootEntities.get(rowNum - 1);
                if (isCollection) {
                    Collection<Object> collection = new ArrayList<>(averageSize);
                    anchorPoint.setValue(rootEntity, collection);
                    collection.add(entity);
                    lastCollection = collection;
                } else {
                    anchorPoint.setValue(rootEntity, entity);
                }
            }

            lastRowNum = rowNum;
        }
    }

}
