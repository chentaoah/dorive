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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.AbstractBinder;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueRouteBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.MultiInBuilder;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StepwiseQueryExecutor extends AbstractQueryExecutor {

    public StepwiseQueryExecutor(AbstractQueryRepository<?, ?> repository) {
        super(repository);
    }

    @Override
    public Result<Object> executeQuery(QueryContext queryContext, Object query) {
        Map<String, QueryUnit> exampleWrapperMap = buildExampleWrapperMap(queryContext);
        executeQuery(queryContext, exampleWrapperMap);
        QueryUnit queryUnit = exampleWrapperMap.get("/");
        boolean abandoned = queryUnit.isAbandoned();
        if (abandoned) {
            return queryContext.newEmptyResult();
        }
        return super.executeQuery(queryContext, queryWrapper);
    }

    private Map<String, QueryUnit> buildExampleWrapperMap(QueryContext queryContext) {
        QueryResolver queryResolver = queryContext.getQueryResolver();
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        Map<String, QueryUnit> exampleWrapperMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : queryResolver.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = new QueryUnit(mergedRepository, example, false);
            exampleWrapperMap.put(relativeAccessPath, queryUnit);
        }
        return exampleWrapperMap;
    }

    private void executeQuery(QueryContext queryContext, Map<String, QueryUnit> exampleWrapperMap) {
        Context context = queryContext.getContext();
        exampleWrapperMap.forEach((accessPath, queryUnit) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = queryUnit.getMergedRepository();
            Example example = queryUnit.getExample();
            boolean abandoned = queryUnit.isAbandoned();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<StrongBinder>> relativeStrongBindersMap = mergedRepository.getRelativeStrongBindersMap();
            Map<String, List<ValueRouteBinder>> relativeValueRouteBindersMap = mergedRepository.getRelativeValueRouteBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            if (!abandoned) {
                abandoned = determineAbandon(exampleWrapperMap, relativeValueRouteBindersMap.keySet());
            }
            if (!abandoned) {
                abandoned = determineAbandon(exampleWrapperMap, relativeStrongBindersMap.keySet());
            }

            List<Object> entities;
            if (abandoned) {
                entities = Collections.emptyList();

            } else if (example.isNotEmpty()) {
                example.select(binderResolver.getSelfFields());
                binderResolver.appendFilterValue(context, example);
                entities = executedRepository.selectByExample(context, example);

            } else {
                return;
            }

            relativeValueRouteBindersMap.forEach((relativeAccessPath, valueRouteBinders) -> {
                QueryUnit targetQueryUnit = exampleWrapperMap.get(relativeAccessPath);
                if (targetQueryUnit != null) {
                    Example targetExample = targetQueryUnit.getExample();
                    for (ValueRouteBinder valueRouteBinder : valueRouteBinders) {
                        Object fieldValue = valueRouteBinder.getFieldValue(context, null);
                        if (fieldValue != null) {
                            String boundName = valueRouteBinder.getBindField();
                            targetExample.eq(boundName, fieldValue);
                        }
                    }
                }
            });

            relativeStrongBindersMap.forEach((relativeAccessPath, strongBinders) -> {
                QueryUnit targetQueryUnit = exampleWrapperMap.get(relativeAccessPath);
                if (targetQueryUnit != null) {
                    if (entities.isEmpty()) {
                        targetQueryUnit.setAbandoned(true);
                        return;
                    }
                    Example targetExample = targetQueryUnit.getExample();
                    if (strongBinders.size() == 1) {
                        StrongBinder strongBinder = strongBinders.get(0);
                        List<Object> fieldValues = collectFieldValues(context, entities, strongBinder);
                        if (!fieldValues.isEmpty()) {
                            String boundName = strongBinder.getBindField();
                            if (fieldValues.size() == 1) {
                                targetExample.eq(boundName, fieldValues.get(0));
                            } else {
                                targetExample.in(boundName, fieldValues);
                            }
                        } else {
                            targetQueryUnit.setAbandoned(true);
                        }

                    } else {
                        List<String> aliases = strongBinders.stream().map(AbstractBinder::getBindFieldAlias).collect(Collectors.toList());
                        MultiInBuilder builder = new MultiInBuilder(aliases, entities.size());
                        collectFieldValues(context, entities, strongBinders, builder);
                        if (!builder.isEmpty()) {
                            targetExample.getCriteria().add(builder.toCriterion());
                        } else {
                            targetQueryUnit.setAbandoned(true);
                        }
                    }
                }
            });
        });
    }

    private boolean determineAbandon(Map<String, QueryUnit> exampleWrapperMap, Set<String> relativeAccessPaths) {
        for (String relativeAccessPath : relativeAccessPaths) {
            QueryUnit targetQueryUnit = exampleWrapperMap.get(relativeAccessPath);
            if (targetQueryUnit != null) {
                if (targetQueryUnit.isAbandoned()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Object> collectFieldValues(Context context, List<Object> entities, StrongBinder strongBinder) {
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

    private void collectFieldValues(Context context, List<Object> entities, List<StrongBinder> strongBinders, MultiInBuilder builder) {
        for (Object entity : entities) {
            for (StrongBinder strongBinder : strongBinders) {
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
