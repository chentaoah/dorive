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

package com.gitee.dorive.coating.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.BuildExample;
import com.gitee.dorive.coating.entity.CoatingCriteria;
import com.gitee.dorive.coating.entity.CoatingType;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.MultiInBuilder;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public DefaultExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public BuildExample buildExample(Context context, Object coating) {
        CoatingType coatingType = repository.getCoatingType(coating);
        CoatingCriteria coatingCriteria = coatingType.newCriteria(coating);
        Map<String, List<Criterion>> criteriaMap = coatingCriteria.getCriteriaMap();
        OrderBy orderBy = coatingCriteria.getOrderBy();
        Page<Object> page = coatingCriteria.getPage();

        Map<String, RepoExample> repoExampleMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : coatingType.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            List<Criterion> criteria = criteriaMap.computeIfAbsent(absoluteAccessPath, key -> new ArrayList<>(2));
            BuildExample buildExample = new BuildExample(criteria);
            repoExampleMap.put(relativeAccessPath, new RepoExample(mergedRepository, buildExample));
        }

        executeQuery(context, repoExampleMap);

        RepoExample repoExample = repoExampleMap.get("/");
        Assert.notNull(repoExample, "The criterion cannot be null!");

        BuildExample buildExample = repoExample.getBuildExample();
        buildExample.setOrderBy(orderBy);
        buildExample.setPage(page);
        return buildExample;
    }

    private void executeQuery(Context context, Map<String, RepoExample> repoExampleMap) {
        repoExampleMap.forEach((accessPath, repoExample) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = repoExample.getMergedRepository();
            BuildExample buildExample = repoExample.getBuildExample();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<PropertyBinder>> mergedBindersMap = mergedRepository.getMergedBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            for (String relativeAccessPath : mergedBindersMap.keySet()) {
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    BuildExample targetExample = targetRepoExample.getBuildExample();
                    if (targetExample.isEmptyQuery()) {
                        buildExample.setEmptyQuery(true);
                        break;
                    }
                }
            }

            if (buildExample.isQueryAll()) {
                return;
            }

            List<Object> entities = Collections.emptyList();
            if (!buildExample.isEmptyQuery() && buildExample.isDirtyQuery()) {
                buildExample.select(binderResolver.getSelfFields());
                entities = executedRepository.selectByExample(context, buildExample);
            }

            for (Map.Entry<String, List<PropertyBinder>> entry : mergedBindersMap.entrySet()) {
                String relativeAccessPath = entry.getKey();
                List<PropertyBinder> binders = entry.getValue();
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    BuildExample targetExample = targetRepoExample.getBuildExample();
                    if (entities.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        return;
                    }
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
                            targetExample.setEmptyQuery(true);
                        }

                    } else {
                        List<String> aliases = binders.stream().map(PropertyBinder::getBindAlias).collect(Collectors.toList());
                        MultiInBuilder builder = new MultiInBuilder(entities.size(), aliases);
                        collectFieldValues(context, entities, binders, builder);
                        if (!builder.isEmpty()) {
                            targetExample.getCriteria().add(builder.build());
                        } else {
                            targetExample.setEmptyQuery(true);
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
        private BuildExample buildExample;
    }

}
