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

package com.gitee.dorive.query.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryField;
import com.gitee.dorive.query.entity.SpecificFields;
import com.gitee.dorive.query.entity.def.CriterionDef;
import com.gitee.dorive.query.entity.def.ExampleDef;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.Data;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class QueryTypeResolver {

    private AbstractQueryRepository<?, ?> repository;
    private Map<Class<?>, QueryResolver> classQueryResolverMap = new ConcurrentHashMap<>();
    private Map<String, QueryResolver> nameQueryResolverMap = new ConcurrentHashMap<>();

    public QueryTypeResolver(AbstractQueryRepository<?, ?> repository) {
        this.repository = repository;
        resolve();
    }

    public void resolve() {
        QueryScanDef queryScanDef = repository.getQueryScanDef();
        Class<?>[] queries = queryScanDef.getQueries();
        for (Class<?> queryClass : queries) {
            resolveQueryClass(queryClass);
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        ExampleDef exampleDef = ExampleDef.fromElement(queryClass);
        if (exampleDef == null) {
            return;
        }

        List<QueryField> queryFields = new ArrayList<>();
        SpecificFields specificFields = new SpecificFields();

        ReflectionUtils.doWithLocalFields(queryClass, declaredField -> {
            QueryField queryField = new QueryField(declaredField);
            if (queryField.isIgnore()) {
                return;
            }
            if (!specificFields.tryAddField(queryField)) {
                queryFields.add(queryField);
            }
        });

        List<MergedRepository> mergedRepositories = matchMergedRepositories(queryFields);
        List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
        Collections.reverse(reversedMergedRepositories);

        QueryResolver queryResolver = new QueryResolver(exampleDef, queryFields, specificFields, mergedRepositories, reversedMergedRepositories);
        classQueryResolverMap.put(queryClass, queryResolver);
        nameQueryResolverMap.put(queryClass.getName(), queryResolver);
    }

    private List<MergedRepository> matchMergedRepositories(List<QueryField> queryFields) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        Set<MergedRepository> mergedRepositorySet = new LinkedHashSet<>();

        for (QueryField queryField : queryFields) {
            CriterionDef criterionDef = queryField.getCriterionDef();
            String belongTo = criterionDef.getBelongTo();
            String field = criterionDef.getField();

            if (!belongTo.startsWith("/")) {
                MergedRepository mergedRepository = nameMergedRepositoryMap.get(belongTo);
                Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);
                belongTo = mergedRepository.getAbsoluteAccessPath();
                criterionDef.setBelongTo(belongTo);
            }
            MergedRepository mergedRepository = mergedRepositoryMap.get(belongTo);
            Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);

            CommonRepository repository = mergedRepository.getExecutedRepository();
            Assert.isTrue(repository.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}", queryField.getField(), repository.getEntityElement().getGenericType(), field);

            mergedRepositorySet.add(mergedRepository);
        }

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            CommonRepository repository = mergedRepository.getExecutedRepository();
            if (repository.isBound()) {
                mergedRepositorySet.add(mergedRepository);
            }
        }

        List<MergedRepository> mergedRepositories = new ArrayList<>(mergedRepositorySet);
        mergedRepositories.sort(Comparator.comparing(MergedRepository::getOrder));
        return mergedRepositories;
    }

}
