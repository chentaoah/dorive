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
package com.gitee.spring.domain.coating.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.coating.api.ExampleBuilder;
import com.gitee.spring.domain.coating.entity.CoatingWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
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

            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            absoluteAccessPath = repositoryDefinition.isDelegateRoot() ? absoluteAccessPath + "/" : absoluteAccessPath;
            repoCriterionMap.put(absoluteAccessPath, repoCriterion);
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

            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String prefixAccessPath = repositoryDefinition.getPrefixAccessPath();
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

            BinderResolver binderResolver = definitionRepository.getBinderResolver();

            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                String absoluteAccessPath = prefixAccessPath + propertyBinder.getBelongAccessPath();
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
                String absoluteAccessPath = prefixAccessPath + propertyBinder.getBelongAccessPath();
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
