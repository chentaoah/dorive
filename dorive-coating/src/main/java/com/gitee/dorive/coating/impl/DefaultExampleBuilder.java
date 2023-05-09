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
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.CoatingCriteria;
import com.gitee.dorive.coating.entity.CoatingType;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiInBuilder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public DefaultExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(Context context, Object coating) {
        CoatingType coatingType = repository.getCoatingType(coating);
        CoatingCriteria coatingCriteria = coatingType.newCriteria(coating);
        Map<String, List<Criterion>> criteriaMap = coatingCriteria.getCriteriaMap();

        Map<String, RepoExample> repoExampleMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : coatingType.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            List<Criterion> criteria = criteriaMap.computeIfAbsent(absoluteAccessPath, key -> new ArrayList<>(2));
            Example example = new Example(criteria);
            repoExampleMap.put(relativeAccessPath, new RepoExample(mergedRepository, example));
        }
        
        executeQuery(context, repoExampleMap);

        RepoExample repoExample = repoExampleMap.get("/");
        Assert.notNull(repoExample, "The criterion cannot be null!");

        Example example = repoExample.getExample();
        example.setOrderBy(coatingCriteria.getOrderBy());
        example.setPage(coatingCriteria.getPage());
        return example;
    }

    private void executeQuery(Context context, Map<String, RepoExample> repoExampleMap) {
        repoExampleMap.forEach((accessPath, repoExample) -> {
            if ("/".equals(accessPath)) return;

            MergedRepository mergedRepository = repoExample.getMergedRepository();
            Example example = repoExample.getExample();

            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            Map<String, List<PropertyBinder>> mergedBindersMap = mergedRepository.getMergedBindersMap();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            for (String relativeAccessPath : mergedBindersMap.keySet()) {
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    Example targetExample = targetRepoExample.getExample();
                    if (targetExample.isEmptyQuery()) {
                        example.setEmptyQuery(true);
                        break;
                    }
                }
            }

            if (example.isQueryAll()) {
                return;
            }

            List<Object> entities = Collections.emptyList();
            if (!example.isEmptyQuery() && example.isDirtyQuery()) {
                example.select(new ArrayList<>(binderResolver.getBoundFields()));
                entities = executedRepository.selectByExample(context, example);
            }

            List<Object> finalEntities = entities;
            mergedBindersMap.forEach((relativeAccessPath, binders) -> {
                RepoExample targetRepoExample = repoExampleMap.get(relativeAccessPath);
                if (targetRepoExample != null) {
                    Example targetExample = targetRepoExample.getExample();
                    if (finalEntities.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        return;
                    }
                    if (binders.size() == 1) {
                        PropertyBinder propertyBinder = binders.get(0);
                        List<Object> fieldValues = collectFieldValues(context, finalEntities, propertyBinder);
                        if (fieldValues.isEmpty()) {
                            targetExample.setEmptyQuery(true);
                            return;
                        }
                        PropChain boundPropChain = propertyBinder.getBoundPropChain();
                        String field = boundPropChain.getEntityField().getName();
                        Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                        fieldValue = propertyBinder.output(context, fieldValue);
                        targetExample.eq(field, fieldValue);

                    } else {
                        List<String> properties = binders.stream()
                                .map(binder -> binder.getBoundPropChain().getEntityField().getName())
                                .collect(Collectors.toList());
                        MultiInBuilder multiInBuilder = new MultiInBuilder(finalEntities.size(), properties);
                        appendFieldValues(context, finalEntities, binders, multiInBuilder);
                        targetExample.getCriteria().add(multiInBuilder.build());
                    }
                }
            });
        });
    }

    private List<Object> collectFieldValues(Context context, List<Object> entities, PropertyBinder propertyBinder) {
        List<Object> fieldValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object fieldValue = propertyBinder.getFieldValue(context, entity);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

    private void appendFieldValues(Context context, List<Object> entities, List<PropertyBinder> binders, MultiInBuilder builder) {
        for (Object entity : entities) {
            for (PropertyBinder binder : binders) {
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
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
    }

}
