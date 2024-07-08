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
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.AbstractBinder;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueRouteBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.MultiInBuilder;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;

import java.util.*;
import java.util.stream.Collectors;

public class StepwiseQueryExecutor extends AbstractQueryExecutor {

    public StepwiseQueryExecutor(AbstractQueryRepository<?, ?> repository) {
        super(repository);
    }

    @Override
    protected List<MergedRepository> getMergedRepositories(Class<?> queryType) {
        QueryTypeResolver queryTypeResolver = repository.getQueryTypeResolver();
        Map<Class<?>, List<MergedRepository>> classReversedMergedRepositoriesMap = queryTypeResolver.getClassReversedMergedRepositoriesMap();
        return classReversedMergedRepositoriesMap.get(queryType);
    }

    @Override
    protected Result<Object> doExecuteQuery(QueryContext queryContext) {
        executeReversedQuery(queryContext);
        QueryUnit queryUnit = queryContext.getQueryUnit();
        if (queryUnit.isAbandoned()) {
            return queryContext.newEmptyResult();
        }
        return super.executeRootQuery(queryContext);
    }

    private void executeReversedQuery(QueryContext queryContext) {
        Context context = queryContext.getContext();
        Map<String, QueryUnit> queryUnitMap = queryContext.getQueryUnitMap();

        queryUnitMap.forEach((accessPath, queryUnit) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = queryUnit.getMergedRepository();
            Example example = queryUnit.getExample();
            boolean abandoned = queryUnit.isAbandoned();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<StrongBinder>> mergedStrongBindersMap = mergedRepository.getMergedStrongBindersMap();
            Map<String, List<ValueRouteBinder>> mergedValueRouteBindersMap = mergedRepository.getMergedValueRouteBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            if (!abandoned) {
                abandoned = determineAbandon(queryUnitMap, mergedValueRouteBindersMap.keySet());
            }
            if (!abandoned) {
                abandoned = determineAbandon(queryUnitMap, mergedStrongBindersMap.keySet());
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

            mergedValueRouteBindersMap.forEach((absoluteAccessPath, valueRouteBinders) -> {
                QueryUnit targetQueryUnit = queryUnitMap.get(absoluteAccessPath);
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

            mergedStrongBindersMap.forEach((absoluteAccessPath, strongBinders) -> {
                QueryUnit targetQueryUnit = queryUnitMap.get(absoluteAccessPath);
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

    private boolean determineAbandon(Map<String, QueryUnit> queryUnitMap, Set<String> absoluteAccessPaths) {
        for (String absoluteAccessPath : absoluteAccessPaths) {
            QueryUnit targetQueryUnit = queryUnitMap.get(absoluteAccessPath);
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
