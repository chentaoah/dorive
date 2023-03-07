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
import com.gitee.dorive.coating.entity.CoatingObj;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.RepositoryObj;
import com.gitee.dorive.coating.impl.resolver.CoatingObjResolver;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.executor.Example;
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

public class DefaultExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public DefaultExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(Context context, Object coating) {
        CoatingObjResolver coatingObjResolver = repository.getCoatingObjResolver();
        Map<Class<?>, CoatingObj> coatingObjMap = coatingObjResolver.getCoatingObjMap();

        CoatingObj coatingObj = coatingObjMap.get(coating.getClass());
        Assert.notNull(coatingObj, "No coating object exists!");

        Map<String, RepoCriterion> repoCriterionMap = new LinkedHashMap<>();
        for (RepositoryObj repositoryObj : coatingObj.getReversedRepositoryObjs()) {
            Example example = repositoryObj.newExampleByCoating(context, coating);
            RepoCriterion repoCriterion = new RepoCriterion(repositoryObj, example);

            MergedRepository mergedRepository = repositoryObj.getMergedRepository();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.isMerged() ? absoluteAccessPath + "/" : absoluteAccessPath;
            repoCriterionMap.put(relativeAccessPath, repoCriterion);
        }

        executeChainQuery(context, repoCriterionMap);

        RepoCriterion repoCriterion = repoCriterionMap.get("/");
        Assert.notNull(repoCriterion, "The criterion cannot be null!");
        return repoCriterion.getExample();
    }

    private void executeChainQuery(Context context, Map<String, RepoCriterion> repoCriterionMap) {
        repoCriterionMap.forEach((accessPath, repoCriterion) -> {
            if ("/".equals(accessPath)) return;

            RepositoryObj repositoryObj = repoCriterion.getRepositoryObj();
            Example example = repoCriterion.getExample();

            MergedRepository mergedRepository = repositoryObj.getMergedRepository();
            String lastAccessPath = mergedRepository.getLastAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

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
                example.selectColumns(new ArrayList<>(binderResolver.getBoundFields()));
                entities = executedRepository.selectByExample(context, example);
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

                    List<Object> fieldValues = collectFieldValues(context, entities, propertyBinder);
                    if (fieldValues.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        continue;
                    }

                    PropChain boundPropChain = propertyBinder.getBoundPropChain();
                    String field = boundPropChain.getEntityField().getName();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                    fieldValue = propertyBinder.output(context, fieldValue);
                    targetExample.eq(field, fieldValue);
                }
            }
        });
    }

    private List<Object> collectFieldValues(Context context, List<Object> entities, PropertyBinder propertyBinder) {
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = propertyBinder.getFieldValue(context, entity);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

    @Data
    @AllArgsConstructor
    public static class RepoCriterion {
        private RepositoryObj repositoryObj;
        private Example example;
    }

}
