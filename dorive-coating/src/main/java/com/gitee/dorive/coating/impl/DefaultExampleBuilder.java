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
import com.gitee.dorive.coating.entity.CoatingWrapper;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.RepositoryWrapper;
import com.gitee.dorive.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.BindingDefinition;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class DefaultExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public DefaultExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        CoatingWrapperResolver coatingWrapperResolver = repository.getCoatingWrapperResolver();
        Map<Class<?>, CoatingWrapper> coatingWrapperMap = coatingWrapperResolver.getCoatingWrapperMap();

        CoatingWrapper coatingWrapper = coatingWrapperMap.get(coatingObject.getClass());
        Assert.notNull(coatingWrapper, "No coating wrapper exists!");

        Map<String, RepoCriterion> repoCriterionMap = new LinkedHashMap<>();
        for (RepositoryWrapper repositoryWrapper : coatingWrapper.getReversedRepositoryWrappers()) {
            Example example = repositoryWrapper.newExampleByCoating(boundedContext, coatingObject);
            RepoCriterion repoCriterion = new RepoCriterion(repositoryWrapper, example);

            MergedRepository mergedRepository = repositoryWrapper.getMergedRepository();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.isMerged() ? absoluteAccessPath + "/" : absoluteAccessPath;
            repoCriterionMap.put(relativeAccessPath, repoCriterion);
        }

        executeChainQuery(boundedContext, repoCriterionMap);

        RepoCriterion repoCriterion = repoCriterionMap.get("/");
        Assert.notNull(repoCriterion, "The criterion cannot be null!");
        return repoCriterion.getExample();
    }

    private void executeChainQuery(BoundedContext boundedContext, Map<String, RepoCriterion> repoCriterionMap) {
        repoCriterionMap.forEach((accessPath, repoCriterion) -> {
            if ("/".equals(accessPath)) return;

            RepositoryWrapper repositoryWrapper = repoCriterion.getRepositoryWrapper();
            Example example = repoCriterion.getExample();

            MergedRepository mergedRepository = repositoryWrapper.getMergedRepository();
            String lastAccessPath = mergedRepository.getLastAccessPath();
            ConfiguredRepository definedRepository = mergedRepository.getDefinedRepository();
            ConfiguredRepository configuredRepository = mergedRepository.getConfiguredRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                String absoluteAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
                RepoCriterion targetRepoCriterion = repoCriterionMap.get(absoluteAccessPath);
                if (targetRepoCriterion != null) {
                    Example targetExample = targetRepoCriterion.getExample();
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
                example.setSelectColumns(binderResolver.getBoundColumns());
                entities = configuredRepository.selectByExample(boundedContext, example);
            }

            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                String absoluteAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
                RepoCriterion targetRepoCriterion = repoCriterionMap.get(absoluteAccessPath);
                if (targetRepoCriterion != null) {
                    Example targetExample = targetRepoCriterion.getExample();
                    if (entities.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        continue;
                    }

                    List<Object> fieldValues = collectFieldValues(boundedContext, entities, propertyBinder);
                    if (fieldValues.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        continue;
                    }

                    BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
                    String bindAlias = bindingDefinition.getBindAlias();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                    fieldValue = propertyBinder.output(boundedContext, fieldValue);
                    targetExample.eq(bindAlias, fieldValue);
                }
            }
        });
    }

    private List<Object> collectFieldValues(BoundedContext boundedContext, List<Object> entities, PropertyBinder propertyBinder) {
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = propertyBinder.getFieldValue(boundedContext, entity);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

    @Data
    @AllArgsConstructor
    public static class RepoCriterion {
        private RepositoryWrapper repositoryWrapper;
        private Example example;
    }

}
