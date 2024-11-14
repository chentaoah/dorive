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
import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.api.entity.query.QueryDefinition;
import com.gitee.dorive.api.entity.query.QueryFieldDefinition;
import com.gitee.dorive.api.entity.query.def.QueryFieldDef;
import com.gitee.dorive.api.impl.query.QueryDefinitionResolver;
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
        RepositoryDef repositoryDef = repository.getRepositoryDef();
        Class<?>[] queries = repositoryDef.getQueries();
        for (Class<?> queryClass : queries) {
            resolveQueryClass(queryClass);
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        QueryDefinitionResolver queryDefinitionResolver = new QueryDefinitionResolver();
        QueryDefinition queryDefinition = queryDefinitionResolver.resolve(queryClass);

        Set<String> accessPaths = new HashSet<>();
        List<MergedRepository> mergedRepositories = new ArrayList<>();
        for (QueryFieldDefinition queryFieldDefinition : queryDefinition.getQueryFieldDefinitions()) {
            MergedRepository mergedRepository = resetQueryField(queryFieldDefinition);
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

        QueryExampleResolver queryExampleResolver = new QueryExampleResolver(queryDefinition);
        classQueryExampleResolverMap.put(queryClass, queryExampleResolver);
        classMergedRepositoriesMap.put(queryClass, mergedRepositories);
        classReversedMergedRepositoriesMap.put(queryClass, reversedMergedRepositories);
    }

    private MergedRepository resetQueryField(QueryFieldDefinition queryFieldDefinition) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        QueryFieldDef queryFieldDef = queryFieldDefinition.getQueryFieldDef();
        String belongTo = queryFieldDef.getBelongTo();
        String field = queryFieldDef.getField();

        if (!belongTo.startsWith("/")) {
            MergedRepository mergedRepository = nameMergedRepositoryMap.get(belongTo);
            Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);
            belongTo = mergedRepository.getAbsoluteAccessPath();
            queryFieldDef.setBelongTo(belongTo);
        }

        MergedRepository mergedRepository = mergedRepositoryMap.get(belongTo);
        Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);

        CommonRepository repository = mergedRepository.getExecutedRepository();
        Assert.isTrue(repository.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                queryFieldDefinition.getField(), repository.getEntityClass(), field);

        return mergedRepository;
    }

}
