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
    private Map<Class<?>, QueryExampleResolver> classQueryExampleResolverMap = new ConcurrentHashMap<>();
    private Map<Class<?>, List<MergedRepository>> classMergedRepositoriesMap = new ConcurrentHashMap<>();
    private Map<Class<?>, List<MergedRepository>> classReversedMergedRepositoriesMap = new ConcurrentHashMap<>();

    public QueryTypeResolver(AbstractQueryRepository<?, ?> repository) {
        this.repository = repository;
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
        Set<String> accessPaths = new HashSet<>();
        List<MergedRepository> mergedRepositories = new ArrayList<>();

        ReflectionUtils.doWithLocalFields(queryClass, declaredField -> {
            QueryField queryField = new QueryField(declaredField);
            if (queryField.isIgnore() || specificFields.tryAddField(queryField)) {
                return;
            }
            MergedRepository mergedRepository = resetQueryField(queryField);
            queryFields.add(queryField);
            if (accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        });

        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            CommonRepository repository = mergedRepository.getExecutedRepository();
            if (repository.isBound() && accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        }

        mergedRepositories.sort(Comparator.comparing(MergedRepository::getOrder));
        List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
        Collections.reverse(reversedMergedRepositories);

        QueryExampleResolver queryExampleResolver = new QueryExampleResolver(exampleDef, queryFields, specificFields);
        classQueryExampleResolverMap.put(queryClass, queryExampleResolver);
        classMergedRepositoriesMap.put(queryClass, mergedRepositories);
        classReversedMergedRepositoriesMap.put(queryClass, reversedMergedRepositories);
    }

    private MergedRepository resetQueryField(QueryField queryField) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

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
        Assert.isTrue(repository.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}", queryField.getField(), repository.getEntityClass(), field);

        return mergedRepository;
    }

}
