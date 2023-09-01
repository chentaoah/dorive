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
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.MultiInBuilder;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.AbstractBinder;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MultiEntityHandler implements EntityHandler {

    private final CommonRepository repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        Map<String, Object> entityIndex = new LinkedHashMap<>(entities.size() * 4 / 3 + 1);
        Example example = newExample(context, entities, entityIndex);
        if (example.isDirtyQuery()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQuery(example);
            query.setType(query.getType() | OperationType.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(context, entities, entityIndex, (MultiResult) result);
            }
            return result.getCount();
        }
        return 0L;
    }

    private Example newExample(Context context, List<Object> entities, Map<String, Object> entityIndex) {
        BinderResolver binderResolver = repository.getBinderResolver();
        Map<String, List<PropertyBinder>> mergedBindersMap = binderResolver.getMergedBindersMap();
        List<PropertyBinder> binders = mergedBindersMap.get("/");

        Example example = new InnerExample();
        if (binders.size() == 1) {
            PropertyBinder binder = binders.get(0);
            List<Object> boundValues = collectBoundValues(context, entities, entityIndex, binder);
            if (!boundValues.isEmpty()) {
                String fieldName = binder.getFieldName();
                if (boundValues.size() == 1) {
                    example.eq(fieldName, boundValues.get(0));
                } else {
                    example.in(fieldName, boundValues);
                }
            }

        } else {
            List<String> aliases = binders.stream().map(AbstractBinder::getAlias).collect(Collectors.toList());
            MultiInBuilder builder = new MultiInBuilder(entities.size(), aliases);
            collectBoundValues(context, entities, entityIndex, binders, builder);
            if (!builder.isEmpty()) {
                example.getCriteria().add(builder.build());
            }
        }

        if (example.isDirtyQuery()) {
            for (ContextBinder binder : binderResolver.getContextBinders()) {
                String fieldName = binder.getFieldName();
                Object boundValue = binder.getBoundValue(context, null);
                if (boundValue != null) {
                    example.eq(fieldName, boundValue);
                }
            }
        }

        return example;
    }

    private List<Object> collectBoundValues(Context context, List<Object> entities, Map<String, Object> entityIndex, PropertyBinder binder) {
        List<Object> boundValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object boundValue = binder.getBoundValue(context, entity);
            if (boundValue != null) {
                boundValue = binder.input(context, boundValue);
                String key = String.valueOf(boundValue);
                if (!entityIndex.containsKey(key)) {
                    boundValues.add(boundValue);
                }
                addToIndex(entityIndex, key, entity);
            }
        }
        return boundValues;
    }

    private void collectBoundValues(Context context, List<Object> entities, Map<String, Object> entityIndex, List<PropertyBinder> binders,
                                    MultiInBuilder multiInBuilder) {
        for (Object entity : entities) {
            StringBuilder strBuilder = new StringBuilder();
            for (PropertyBinder binder : binders) {
                Object boundValue = binder.getBoundValue(context, entity);
                if (boundValue != null) {
                    boundValue = binder.input(context, boundValue);
                    multiInBuilder.append(boundValue);
                    strBuilder.append(boundValue).append(",");
                } else {
                    multiInBuilder.clear();
                    strBuilder = null;
                    break;
                }
            }
            if (strBuilder != null) {
                if (strBuilder.length() > 0) {
                    strBuilder.deleteCharAt(strBuilder.length() - 1);
                }
                String key = strBuilder.toString();
                if (entityIndex.containsKey(key)) {
                    multiInBuilder.clear();
                }
                addToIndex(entityIndex, key, entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addToIndex(Map<String, Object> entityIndex, String key, Object entity) {
        Object object = entityIndex.get(key);
        if (object instanceof Collection) {
            ((Collection<Object>) object).add(entity);

        } else if (object != null) {
            List<Object> entities = new ArrayList<>(4);
            entities.add(object);
            entities.add(entity);
            entityIndex.put(key, entities);

        } else {
            entityIndex.put(key, entity);
        }
    }

    @SuppressWarnings("unchecked")
    private void setValueForRootEntities(Context context, List<Object> rootEntities, Map<String, Object> entityIndex, MultiResult multiResult) {
        boolean isCollection = repository.getEntityEle().isCollection();
        PropChain anchorPoint = repository.getAnchorPoint();

        BinderResolver binderResolver = repository.getBinderResolver();
        List<PropertyBinder> binders = binderResolver.getMergedBindersMap().get("/");

        List<Object> entities = multiResult.getRecords();
        int averageSize = entities.size() / rootEntities.size() + 1;

        for (Object entity : entities) {
            StringBuilder strBuilder = new StringBuilder();
            if (binders.size() == 1) {
                PropertyBinder binder = binders.get(0);
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    strBuilder.append(fieldValue);
                } else {
                    strBuilder = null;
                }
            } else {
                for (PropertyBinder binder : binders) {
                    Object fieldValue = binder.getFieldValue(context, entity);
                    if (fieldValue != null) {
                        strBuilder.append(fieldValue).append(",");
                    } else {
                        strBuilder = null;
                        break;
                    }
                }
                if (strBuilder != null && strBuilder.length() > 0) {
                    strBuilder.deleteCharAt(strBuilder.length() - 1);
                }
            }
            if (strBuilder != null) {
                Object object = entityIndex.get(strBuilder.toString());
                if (object instanceof Collection) {
                    for (Object rootEntity : (Collection<Object>) object) {
                        setValueForRootEntity(isCollection, anchorPoint, averageSize, rootEntity, entity);
                    }
                } else {
                    setValueForRootEntity(isCollection, anchorPoint, averageSize, object, entity);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setValueForRootEntity(boolean isCollection, PropChain anchorPoint, int averageSize, Object rootEntity, Object entity) {
        if (rootEntity != null) {
            Object value = anchorPoint.getValue(rootEntity);
            if (isCollection) {
                Collection<Object> collection;
                if (value == null) {
                    collection = new ArrayList<>(averageSize);
                    anchorPoint.setValue(rootEntity, collection);
                } else {
                    collection = (Collection<Object>) value;
                }
                collection.add(entity);

            } else {
                if (value == null) {
                    anchorPoint.setValue(rootEntity, entity);
                }
            }
        }
    }

}
