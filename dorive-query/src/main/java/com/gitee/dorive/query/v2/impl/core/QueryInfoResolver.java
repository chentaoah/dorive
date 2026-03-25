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

package com.gitee.dorive.query.v2.impl.core;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import com.gitee.dorive.base.v1.aggregate.api.QueryTypeResolver;
import com.gitee.dorive.base.v1.common.def.QueryFieldDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.common.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query.v2.entity.core.QueryInfo;
import com.gitee.dorive.query.v2.entity.core.QueryRepositoryMapping;
import com.gitee.dorive.query.v2.entity.core.RepositoryInfo;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class QueryInfoResolver {

    private RepositoryContext repositoryContext;
    private Map<Class<?>, QueryDefinition> classQueryDefinitionMap = new ConcurrentHashMap<>();
    private Map<Class<?>, QueryInfo> classQueryInfoMap = new ConcurrentHashMap<>();

    public QueryInfoResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
        resolve();
    }

    private void resolve() {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        Class<?>[] queries = repositoryDef.getQueries();
        if (queries != null && queries.length > 0) {
            for (Class<?> queryClass : queries) {
                resolveQueryClass(queryClass);
            }
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        QueryTypeResolver queryTypeResolver = SpringUtil.getBean(QueryTypeResolver.class);
        QueryDefinition queryDefinition = queryTypeResolver.resolve(queryClass);
        classQueryDefinitionMap.put(queryClass, queryDefinition);

        Map<RepositoryInfo, QueryRepositoryMapping> queryRepositoryMappingMap = new LinkedHashMap<>();
        for (QueryFieldDefinition queryField : queryDefinition.getQueryFieldDefinitions()) {
            List<RepositoryInfo> repositoryInfos = resetQueryField(queryField);
            for (RepositoryInfo repositoryInfo : repositoryInfos) {
                QueryRepositoryMapping queryRepositoryMapping = queryRepositoryMappingMap.computeIfAbsent(repositoryInfo, k -> new QueryRepositoryMapping(repositoryInfo, new ArrayList<>()));
                queryRepositoryMapping.getQueryFields().add(queryField);
            }
        }

        // 向上遍历
        for (RepositoryInfo repositoryInfo : queryRepositoryMappingMap.keySet()) {
            RepositoryInfo parent = repositoryInfo.getParent();
            while (parent != null) {
                if (!queryRepositoryMappingMap.containsKey(parent)) {
                    queryRepositoryMappingMap.put(parent, new QueryRepositoryMapping(parent, new ArrayList<>()));
                }
                parent = parent.getParent();
            }
        }

        // 重新排序
        List<QueryRepositoryMapping> queryRepositoryMappings = new ArrayList<>(queryRepositoryMappingMap.values());
        queryRepositoryMappings.sort(Comparator.comparing(q -> q.getRepositoryInfo().getSequence()));
        // 反转顺序
        List<QueryRepositoryMapping> reversedQueryRepositoryMappings = new ArrayList<>(queryRepositoryMappings);
        Collections.reverse(reversedQueryRepositoryMappings);
        // 条件
        ExampleResolver exampleResolver = new ExampleResolver(queryDefinition);

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQueryRepositoryMappings(queryRepositoryMappings);
        queryInfo.setReversedQueryRepositoryMappings(reversedQueryRepositoryMappings);
        queryInfo.setExampleResolver(exampleResolver);
        classQueryInfoMap.put(queryClass, queryInfo);
    }

    private List<RepositoryInfo> resetQueryField(QueryFieldDefinition queryFieldDefinition) {
        RepositoryInfoResolver repositoryInfoResolver = repositoryContext.getProperty(RepositoryInfoResolver.class);
        Map<String, RepositoryInfo> pathRepositoryInfoMap = repositoryInfoResolver.getPathRepositoryInfoMap();
        Map<Class<?>, List<String>> classPathsMap = repositoryInfoResolver.getClassPathsMap();
        Map<String, List<String>> namePathsMap = repositoryInfoResolver.getNamePathsMap();

        QueryFieldDef queryFieldDef = queryFieldDefinition.getQueryFieldDef();
        Field javaField = queryFieldDefinition.getField();

        String[] path = queryFieldDef.getPath();
        Class<?> entity = queryFieldDef.getEntity();
        String name = queryFieldDef.getName();
        String field = queryFieldDef.getField();

        // 路径 > 类型 > 名称
        List<String> paths = Collections.emptyList();
        if (ArrayUtils.isNotEmpty(path)) {
            paths = Arrays.asList(path);

        } else if (entity != Object.class) {
            paths = classPathsMap.get(entity);
            Assert.notEmpty(paths, "No merged repository found! entity: {}", entity.getName());

        } else if (StringUtils.isNotBlank(name)) {
            paths = namePathsMap.get(name);
            Assert.notEmpty(paths, "No merged repository found! name: {}", name);
        }
        queryFieldDef.setPath(paths.toArray(new String[0]));

        List<RepositoryInfo> repositoryInfos = new ArrayList<>();
        for (String eachPath : paths) {
            RepositoryInfo repositoryInfo = pathRepositoryInfoMap.get(eachPath);
            Assert.notNull(repositoryInfo, "No merged repository found! path: {}", eachPath);

            RepositoryContext repositoryContext = repositoryInfo.getRepositoryContext();
            EntityElement entityElement = repositoryContext.getEntityElement();
            Class<?> entityClass = repositoryContext.getEntityClass();
            Assert.isTrue(entityElement.hasField(field),
                    "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                    javaField, entityClass, field);

            repositoryInfos.add(repositoryInfo);
        }
        return repositoryInfos;
    }

    public QueryInfo findQueryInfo(Class<?> queryClass) {
        return classQueryInfoMap.get(queryClass);
    }

}
