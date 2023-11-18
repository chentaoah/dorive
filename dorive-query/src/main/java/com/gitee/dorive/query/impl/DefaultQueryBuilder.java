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

package com.gitee.dorive.query.impl;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.MultiInBuilder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.api.QueryBuilder;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryCtx;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultQueryBuilder implements QueryBuilder {

    @Override
    public QueryCtx build(Context context, Object query) {
        QueryCtx queryCtx = (QueryCtx) query;

        Map<String, ExampleWrapper> exampleWrapperMap = buildExampleWrapperMap(queryCtx);
        executeQuery(context, exampleWrapperMap);

        ExampleWrapper exampleWrapper = exampleWrapperMap.get("/");
        queryCtx.setAbandoned(exampleWrapper.isAbandoned());

        return queryCtx;
    }

    private Map<String, ExampleWrapper> buildExampleWrapperMap(QueryCtx queryCtx) {
        QueryResolver queryResolver = queryCtx.getQueryResolver();
        Map<String, Example> exampleMap = queryCtx.getExampleMap();
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

    private void executeQuery(Context context, Map<String, ExampleWrapper> exampleWrapperMap) {
        exampleWrapperMap.forEach((accessPath, exampleWrapper) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = exampleWrapper.getMergedRepository();
            Example example = exampleWrapper.getExample();
            boolean abandoned = exampleWrapper.isAbandoned();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<PropertyBinder>> mergedBindersMap = mergedRepository.getMergedBindersMap();
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

            if (!abandoned && example.isEmpty()) {
                return;
            }

            List<Object> entities = Collections.emptyList();
            if (!abandoned && example.isNotEmpty()) {
                example.select(binderResolver.getSelfFields());
                entities = executedRepository.selectByExample(context, example);
            }

            for (Map.Entry<String, List<PropertyBinder>> entry : mergedBindersMap.entrySet()) {
                String relativeAccessPath = entry.getKey();
                List<PropertyBinder> binders = entry.getValue();
                ExampleWrapper targetExampleWrapper = exampleWrapperMap.get(relativeAccessPath);
                if (targetExampleWrapper != null) {
                    if (entities.isEmpty()) {
                        targetExampleWrapper.setAbandoned(true);
                        return;
                    }
                    Example targetExample = targetExampleWrapper.getExample();
                    if (binders.size() == 1) {
                        PropertyBinder binder = binders.get(0);
                        List<Object> fieldValues = binder.collectFieldValues(context, entities);
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
                        List<String> aliases = binders.stream().map(PropertyBinder::getBindAlias).collect(Collectors.toList());
                        MultiInBuilder builder = new MultiInBuilder(entities.size(), aliases);
                        collectFieldValues(context, entities, binders, builder);
                        if (!builder.isEmpty()) {
                            targetExample.getCriteria().add(builder.build());
                        } else {
                            targetExampleWrapper.setAbandoned(true);
                        }
                    }
                }
            }
        });
    }

    private void collectFieldValues(Context context, List<Object> entities, List<PropertyBinder> binders, MultiInBuilder builder) {
        for (Object entity : entities) {
            for (PropertyBinder binder : binders) {
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    fieldValue = binder.output(context, fieldValue);
                    builder.append(fieldValue);
                } else {
                    builder.clear();
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
