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

package com.gitee.dorive.query2.v1.impl.stepwise;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultContext;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.executor.util.MultiInBuilder;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class StepwiseQuerier {

    private final RepositoryContext repositoryContext;
    private final List<RepositoryItem> reverseSubRepositoryItems;

    public StepwiseQuerier(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
        List<RepositoryItem> repositoryItems = new ArrayList<>(repositoryContext.getSubRepositories());
        Collections.reverse(repositoryItems);
        this.reverseSubRepositoryItems = repositoryItems;
    }

    public Example executeQuery(Context context, Map<String, Example> exampleMap) {
        for (RepositoryItem repositoryItem : reverseSubRepositoryItems) {
            // 获取查询条件
            String accessPath = repositoryItem.getAccessPath();
            Example example = exampleMap.get(accessPath);
            if (example == null || example.isEmpty()) {
                continue;
            }
            // 获取绑定关系
            BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
            Map<String, List<Binder>> mergedStrongBindersMap = binderExecutor.getMergedStrongBindersMap();
            Map<String, List<Binder>> mergedValueRouteBindersMap = binderExecutor.getMergedValueRouteBindersMap();
            // 判断是否放弃
            boolean abandoned = example.isAbandoned();
            if (!abandoned) {
                abandoned = determineAbandon(exampleMap, mergedValueRouteBindersMap.keySet());
            }
            if (!abandoned) {
                abandoned = determineAbandon(exampleMap, mergedStrongBindersMap.keySet());
            }
            // 查询
            List<Object> entities;
            if (abandoned) {
                entities = Collections.emptyList();

            } else if (example.isNotEmpty()) {
                example.select(binderExecutor.getSelfFields());
                binderExecutor.appendFilterValue(context, example);
                entities = repositoryItem.selectByExample(new DefaultContext(Options.ROOT, context), example);

            } else {
                continue;
            }
            // 条件
            mergedValueRouteBindersMap.forEach((targetAccessPath, valueRouteBinders) -> {
                Example targetExample = exampleMap.computeIfAbsent(targetAccessPath, k -> new InnerExample());
                for (Binder valueRouteBinder : valueRouteBinders) {
                    Object fieldValue = valueRouteBinder.getFieldValue(context, null);
                    if (fieldValue != null) {
                        String boundName = valueRouteBinder.getBindField();
                        targetExample.eq(boundName, fieldValue);
                    }
                }
            });
            // 条件
            mergedStrongBindersMap.forEach((targetAccessPath, strongBinders) -> {
                Example targetExample = exampleMap.computeIfAbsent(targetAccessPath, k -> new InnerExample());
                if (entities.isEmpty()) {
                    targetExample.setAbandoned(true);
                    return;
                }
                if (strongBinders.size() == 1) {
                    Binder strongBinder = strongBinders.get(0);
                    List<Object> fieldValues = collectFieldValues(context, entities, strongBinder);
                    if (fieldValues.isEmpty()) {
                        targetExample.setAbandoned(true);
                        return;
                    }
                    String boundName = strongBinder.getBindField();
                    if (fieldValues.size() == 1) {
                        targetExample.eq(boundName, fieldValues.get(0));
                    } else {
                        targetExample.in(boundName, fieldValues);
                    }

                } else {
                    List<String> properties = strongBinders.stream().map(Binder::getBindField).collect(Collectors.toList());
                    MultiInBuilder builder = new MultiInBuilder(properties, entities.size());
                    collectFieldValues(context, entities, strongBinders, builder);
                    if (builder.isEmpty()) {
                        targetExample.setAbandoned(true);
                        return;
                    }
                    targetExample.getCriteria().add(builder.toCriterion());
                }
            });
        }
        return exampleMap.get("/");
    }

    private boolean determineAbandon(Map<String, Example> exampleMap, Set<String> accessPaths) {
        for (String accessPath : accessPaths) {
            Example example = exampleMap.get(accessPath);
            if (example != null) {
                if (example.isAbandoned()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Object> collectFieldValues(Context context, List<Object> entities, Binder strongBinder) {
        List<Object> fieldValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object fieldValue = strongBinder.getFieldValue(context, entity);
            if (fieldValue != null) {
                fieldValue = strongBinder.output(context, fieldValue);
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

    private void collectFieldValues(Context context, List<Object> entities, List<Binder> strongBinders, MultiInBuilder builder) {
        for (Object entity : entities) {
            for (Binder strongBinder : strongBinders) {
                Object fieldValue = strongBinder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    fieldValue = strongBinder.output(context, fieldValue);
                    builder.append(fieldValue);
                } else {
                    builder.clearRemainder();
                    break;
                }
            }
        }
    }

}
