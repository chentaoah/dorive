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

import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class UnionEntityHandler implements EntityHandler {

    private final CommonRepository repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        Example example = newExample(context, entities);
        if (example.isNotEmpty()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQueryByExample(example);
            query.setRootType(Operation.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(entities, (MultiResult) result);
            }
            return result.getCount();
        }
        return 0L;
    }

    private Example newExample(Context context, List<Object> entities) {
        PropChain anchorPoint = repository.getAnchorPoint();
        PropChain lastPropChain = anchorPoint.getLastPropChain();
        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < entities.size(); index++) {
            Object entity = entities.get(index);
            Object lastEntity = lastPropChain == null ? entity : lastPropChain.getValue(entity);
            if (lastEntity != null) {
                Example example = newExample(context, entity);
                if (example.isNotEmpty()) {
                    example.selectExtra((index + 1) + " as $row");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    private Example newExample(Context context, Object entity) {
        BinderResolver binderResolver = repository.getBinderResolver();
        Example example = new InnerExample();
        for (PropertyBinder binder : binderResolver.getPropertyBinders()) {
            String fieldName = binder.getFieldName();
            Object boundValue = binder.getBoundValue(context, entity);
            if (boundValue instanceof Collection) {
                boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
            }
            if (boundValue != null) {
                boundValue = binder.input(context, boundValue);
                example.eq(fieldName, boundValue);
            } else {
                example.getCriteria().clear();
                break;
            }
        }
        if (example.isNotEmpty()) {
            for (ContextBinder binder : binderResolver.getContextBinders()) {
                String fieldName = binder.getFieldName();
                Object boundValue = binder.getBoundValue(context, entity);
                if (boundValue != null) {
                    example.eq(fieldName, boundValue);
                }
            }
        }
        return example;
    }

    private void setValueForRootEntities(List<Object> rootEntities, MultiResult multiResult) {
        boolean isCollection = repository.getEntityEle().isCollection();
        PropChain anchorPoint = repository.getAnchorPoint();

        List<Map<String, Object>> resultMaps = multiResult.getResultMaps();
        int averageSize = resultMaps.size() / rootEntities.size() + 1;
        int lastRowNum = -1;
        Collection<Object> lastCollection = null;

        for (Map<String, Object> resultMap : resultMaps) {
            Integer rowNum = NumberUtils.intValue(resultMap.get("$row"));
            Object entity = resultMap.get("$entity");

            if (rowNum == lastRowNum) {
                if (isCollection && lastCollection != null) {
                    lastCollection.add(entity);
                }

            } else {
                Object rootEntity = rootEntities.get(rowNum - 1);
                if (isCollection) {
                    Collection<Object> collection = new ArrayList<>(averageSize);
                    collection.add(entity);
                    lastCollection = collection;
                    anchorPoint.setValue(rootEntity, collection);
                } else {
                    anchorPoint.setValue(rootEntity, entity);
                }
            }

            lastRowNum = rowNum;
        }
    }

}
