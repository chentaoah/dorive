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
import com.gitee.dorive.core.entity.executor.Criterion;
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

import java.util.*;
import java.util.stream.Collectors;

public class DefaultQueryBuilder implements QueryBuilder {

    @Override
    public QueryCtx build(Context context, Object query) {
        QueryCtx queryCtx = (QueryCtx) query;
        QueryResolver queryResolver = queryCtx.getQueryResolver();
        Map<String, List<Criterion>> criteriaMap = queryCtx.getCriteriaMap();

        Map<String, RepoExample> repoExampleMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : queryResolver.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            List<Criterion> criteria = criteriaMap.computeIfAbsent(absoluteAccessPath, key -> new ArrayList<>(2));
            Example example = new InnerExample(criteria);
            repoExampleMap.put(relativeAccessPath, new RepoExample(mergedRepository, example, false));
        }
        executeQuery(context, repoExampleMap);

        RepoExample repoExample = repoExampleMap.get("/");
        queryCtx.getExample().setCriteria(repoExample.getExample().getCriteria());
        queryCtx.setAbandoned(repoExample.isAbandoned());
        return queryCtx;
    }

    private void executeQuery(Context context, Map<String, RepoExample> repoExampleMap) {
        repoExampleMap.forEach((accessPath, repoExample) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = repoExample.getMergedRepository();
            Example example = repoExample.getExample();
            boolean abandoned = repoExample.isAbandoned();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<PropertyBinder>> mergedBindersMap = mergedRepository.getMergedBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            for (String relativeAccessPath : mergedBindersMap.keySet()) {
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    if (targetRepoExample.isAbandoned()) {
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
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    if (entities.isEmpty()) {
                        targetRepoExample.setAbandoned(true);
                        return;
                    }
                    Example targetExample = targetRepoExample.getExample();
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
                            targetRepoExample.setAbandoned(true);
                        }

                    } else {
                        List<String> aliases = binders.stream().map(PropertyBinder::getBindAlias).collect(Collectors.toList());
                        MultiInBuilder builder = new MultiInBuilder(entities.size(), aliases);
                        collectFieldValues(context, entities, binders, builder);
                        if (!builder.isEmpty()) {
                            targetExample.getCriteria().add(builder.build());
                        } else {
                            targetRepoExample.setAbandoned(true);
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
    public static class RepoExample {
        private MergedRepository mergedRepository;
        private Example example;
        private boolean abandoned;
    }

}
