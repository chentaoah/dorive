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

package com.gitee.dorive.query.v1.impl.resolver;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import com.gitee.dorive.base.v1.aggregate.api.QueryResolver;
import com.gitee.dorive.base.v1.common.def.QueryFieldDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.common.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query.v1.entity.MergedRepository;
import com.gitee.dorive.query.v1.entity.QueryConfig;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class QueryTypeResolver {

    private RepositoryContext repository;
    private Map<Class<?>, QueryConfig> classQueryConfigMap = new ConcurrentHashMap<>();

    public QueryTypeResolver(RepositoryContext repository) {
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
        QueryResolver queryResolver = SpringUtil.getBean(QueryResolver.class);
        QueryDefinition queryDefinition = queryResolver.resolve(queryClass);
        QueryExampleResolver queryExampleResolver = new QueryExampleResolver(queryDefinition);

        Set<String> accessPaths = new HashSet<>();
        List<MergedRepository> mergedRepositories = new ArrayList<>();
        for (QueryFieldDefinition queryFieldDefinition : queryDefinition.getQueryFieldDefinitions()) {
            MergedRepository mergedRepository = resetQueryField(queryFieldDefinition);
            if (accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        }

        // 添加绑定的且未添加到集合的仓储
        MergedRepositoryResolver mergedRepositoryResolver = repository.getProperty(MergedRepositoryResolver.class);
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            RepositoryItem repository = mergedRepository.getExecutedRepository();
            if (repository.isBound() && accessPaths.add(mergedRepository.getAbsoluteAccessPath())) {
                mergedRepositories.add(mergedRepository);
            }
        }
        // 重新排序
        mergedRepositories.sort(Comparator.comparing(MergedRepository::getSequence));
        // 反转顺序
        List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
        Collections.reverse(reversedMergedRepositories);

        QueryConfig queryConfig = new QueryConfig();
        queryConfig.setRepository(repository);
        queryConfig.setQueryExampleResolver(queryExampleResolver);
        queryConfig.setMergedRepositories(mergedRepositories);
        queryConfig.setReversedMergedRepositories(reversedMergedRepositories);
        classQueryConfigMap.put(queryClass, queryConfig);
    }

    private MergedRepository resetQueryField(QueryFieldDefinition queryFieldDefinition) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getProperty(MergedRepositoryResolver.class);
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<Class<?>, MergedRepository> classMergedRepositoryMap = mergedRepositoryResolver.getClassMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        QueryFieldDef queryFieldDef = queryFieldDefinition.getQueryFieldDef();
        String path = ArrayUtils.isNotEmpty(queryFieldDef.getPath()) ? queryFieldDef.getPath()[0] : "";
        Class<?> entity = queryFieldDef.getEntity();
        String name = queryFieldDef.getName();
        String field = queryFieldDef.getField();

        // 路径 > 类型 > 名称
        if (StringUtils.isBlank(path)) {
            MergedRepository mergedRepository = null;
            if (entity != Object.class) {
                mergedRepository = classMergedRepositoryMap.get(entity);
                Assert.notNull(mergedRepository, "No merged repository found! entity: {}", entity.getName());

            } else if (StringUtils.isNotBlank(name)) {
                mergedRepository = nameMergedRepositoryMap.get(name);
                Assert.notNull(mergedRepository, "No merged repository found! name: {}", name);
            }
            if (mergedRepository != null) {
                path = mergedRepository.getAbsoluteAccessPath();
                queryFieldDef.setPath(new String[]{path});
            }
        }

        MergedRepository mergedRepository = mergedRepositoryMap.get(path);
        Assert.notNull(mergedRepository, "No merged repository found! path: {}", path);

        RepositoryItem repository = mergedRepository.getExecutedRepository();
        Assert.isTrue(repository.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                queryFieldDefinition.getField(), repository.getEntityClass(), field);

        return mergedRepository;
    }

}
