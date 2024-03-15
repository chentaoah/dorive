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

package com.gitee.dorive.query.impl.executor;

import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.BoundBinder;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.MultiInBuilder;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryWrapper;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StepwiseQueryExecutor extends AbstractQueryExecutor {

    public StepwiseQueryExecutor(AbstractQueryRepository<?, ?> repository) {
        super(repository);
    }

    @Override
    public Result<Object> executeQuery(QueryContext queryContext, QueryWrapper queryWrapper) {
        Map<String, ExampleWrapper> exampleWrapperMap = buildExampleWrapperMap(queryContext);
        executeQuery(queryContext, exampleWrapperMap);
        ExampleWrapper exampleWrapper = exampleWrapperMap.get("/");
        boolean abandoned = exampleWrapper.isAbandoned();
        if (abandoned) {
            return queryContext.newEmptyResult();
        }
        return super.executeQuery(queryContext, queryWrapper);
    }

    private Map<String, ExampleWrapper> buildExampleWrapperMap(QueryContext queryContext) {
        QueryResolver queryResolver = queryContext.getQueryResolver();
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        Map<String, ExampleWrapper> exampleWrapperMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : queryResolver.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            ExampleWrapper exampleWrapper = new ExampleWrapper(mergedRepository, example, false);
            exampleWrapperMap.put(relativeAccessPath, exampleWrapper);
        }
        return exampleWrapperMap;
    }

    private void executeQuery(QueryContext queryContext, Map<String, ExampleWrapper> exampleWrapperMap) {
        Context context = queryContext.getContext();
        exampleWrapperMap.forEach((accessPath, exampleWrapper) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = exampleWrapper.getMergedRepository();
            Example example = exampleWrapper.getExample();
            boolean abandoned = exampleWrapper.isAbandoned();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<Binder>> mergedBindersMap = mergedRepository.getMergedBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            for (String relativeAccessPath : mergedBindersMap.keySet()) {
                ExampleWrapper targetExampleWrapper = exampleWrapperMap.get(relativeAccessPath);
                if (targetExampleWrapper != null) {
                    if (targetExampleWrapper.isAbandoned()) {
                        abandoned = true;
                        break;
                    }
                }
            }

            List<Object> entities;
            if (abandoned) {
                entities = Collections.emptyList();

            } else if (example.isNotEmpty()) {
                example.select(binderResolver.getSelfFields());
                entities = executedRepository.selectByExample(context, example);

            } else {
                return;
            }

            for (Map.Entry<String, List<Binder>> entry : mergedBindersMap.entrySet()) {
                String relativeAccessPath = entry.getKey();
                List<Binder> binders = entry.getValue();
                ExampleWrapper targetExampleWrapper = exampleWrapperMap.get(relativeAccessPath);
                if (targetExampleWrapper != null) {
                    if (entities.isEmpty()) {
                        targetExampleWrapper.setAbandoned(true);
                        return;
                    }
                    Example targetExample = targetExampleWrapper.getExample();
                    if (binders.size() == 1) {
                        Binder binder = binders.get(0);
                        List<Object> fieldValues = collectFieldValues(context, entities, binder);
                        if (!fieldValues.isEmpty()) {
                            String boundName = binder.getBoundName();
                            if (fieldValues.size() == 1) {
                                targetExample.eq(boundName, fieldValues.get(0));
                            } else {
                                targetExample.in(boundName, fieldValues);
                            }
                        } else {
                            targetExampleWrapper.setAbandoned(true);
                        }

                    } else {
                        List<String> aliases = binders.stream().map(binder -> {
                            if (binder instanceof BoundBinder) {
                                return ((BoundBinder) binder).getBindAlias();

                            } else if (binder instanceof StrongBinder) {
                                BoundBinder boundBinder = ((StrongBinder) binder).getBoundBinder();
                                return boundBinder.getBindAlias();
                            }
                            return null;
                        }).collect(Collectors.toList());
                        MultiInBuilder builder = new MultiInBuilder(aliases, entities.size());
                        collectFieldValues(context, entities, binders, builder);
                        if (!builder.isEmpty()) {
                            targetExample.getCriteria().add(builder.toCriterion());
                        } else {
                            targetExampleWrapper.setAbandoned(true);
                        }
                    }
                }
            }
        });
    }

    private List<Object> collectFieldValues(Context context, List<Object> entities, Binder binder) {
        List<Object> fieldValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object fieldValue = binder.getFieldValue(context, entity);
            if (fieldValue != null) {
                fieldValue = binder.output(context, fieldValue);
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

    private void collectFieldValues(Context context, List<Object> entities, List<Binder> binders, MultiInBuilder builder) {
        for (Object entity : entities) {
            for (Binder binder : binders) {
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    fieldValue = binder.output(context, fieldValue);
                    builder.append(fieldValue);
                } else {
                    builder.clearRemainder();
                    break;
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class ExampleWrapper {
        private MergedRepository mergedRepository;
        private Example example;
        private boolean abandoned;
    }

}
