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
import com.gitee.dorive.api.entity.query.QueryDefinition;
import com.gitee.dorive.api.entity.query.QueryFieldDefinition;
import com.gitee.dorive.api.entity.query.def.EnableQueryDef;
import com.gitee.dorive.api.entity.query.ele.QueryElement;
import com.gitee.dorive.api.entity.query.ele.QueryFieldElement;
import com.gitee.dorive.api.impl.query.QueryDefinitionReader;
import com.gitee.dorive.api.impl.query.QueryElementResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.Data;

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
        EnableQueryDef enableQueryDef = repository.getEnableQueryDef();
        List<Class<?>> queries = enableQueryDef.getQueries();
        for (Class<?> queryClass : queries) {
            resolveQueryClass(queryClass);
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        Class<?> entityClass = repository.getEntityClass();

        QueryDefinitionReader queryDefinitionReader = new QueryDefinitionReader();
        QueryDefinition queryDefinition = queryDefinitionReader.read(entityClass, queryClass);

        QueryElementResolver queryElementResolver = new QueryElementResolver();
        QueryElement queryElement = queryElementResolver.resolve(queryDefinition);

        Set<String> accessPaths = new HashSet<>();
        List<MergedRepository> mergedRepositories = new ArrayList<>();
        for (QueryFieldElement queryFieldElement : queryElement.getQueryFieldElements()) {
            MergedRepository mergedRepository = resetQueryField(queryFieldElement);
            if (accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        }

        // 添加绑定的且未添加到集合的仓储
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            CommonRepository repository = mergedRepository.getExecutedRepository();
            if (repository.isBound() && accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        }
        // 重新排序
        mergedRepositories.sort(Comparator.comparing(MergedRepository::getSequence));
        // 反转顺序
        List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
        Collections.reverse(reversedMergedRepositories);

        QueryExampleResolver queryExampleResolver = new QueryExampleResolver(queryElement);
        classQueryExampleResolverMap.put(queryClass, queryExampleResolver);
        classMergedRepositoriesMap.put(queryClass, mergedRepositories);
        classReversedMergedRepositoriesMap.put(queryClass, reversedMergedRepositories);
    }

    private MergedRepository resetQueryField(QueryFieldElement queryFieldElement) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        QueryFieldDefinition queryFieldDefinition = queryFieldElement.getQueryFieldDefinition();
        String belongTo = queryFieldDefinition.getBelongTo();
        String field = queryFieldDefinition.getField();

        if (!belongTo.startsWith("/")) {
            MergedRepository mergedRepository = nameMergedRepositoryMap.get(belongTo);
            Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);
            belongTo = mergedRepository.getAbsoluteAccessPath();
            queryFieldDefinition.setBelongTo(belongTo);
        }

        MergedRepository mergedRepository = mergedRepositoryMap.get(belongTo);
        Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);

        CommonRepository repository = mergedRepository.getExecutedRepository();
        Assert.isTrue(repository.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                queryFieldElement.getField(), repository.getEntityClass(), field);

        return mergedRepository;
    }

}
