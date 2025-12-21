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

package com.gitee.dorive.query2.v1.impl.core;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import com.gitee.dorive.base.v1.common.def.QueryFieldDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.common.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query2.v1.entity.QueryConfig;
import com.gitee.dorive.query2.v1.entity.QueryNode;
import com.gitee.dorive.query2.v1.entity.RepositoryNode;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class QueryConfigResolver {

    private RepositoryContext repositoryContext;
    private Map<Class<?>, QueryConfig> classQueryConfigMap = new ConcurrentHashMap<>();

    public QueryConfigResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
    }

    public void resolve() {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        Class<?>[] queries = repositoryDef.getQueries();
        if (queries != null && queries.length > 0) {
            for (Class<?> queryClass : queries) {
                resolveQueryClass(queryClass);
            }
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        com.gitee.dorive.base.v1.aggregate.api.QueryResolver queryResolver = SpringUtil.getBean(com.gitee.dorive.base.v1.aggregate.api.QueryResolver.class);
        QueryDefinition queryDefinition = queryResolver.resolve(queryClass);

        Map<RepositoryNode, QueryNode> queryNodeMap = new LinkedHashMap<>();
        for (QueryFieldDefinition queryField : queryDefinition.getQueryFieldDefinitions()) {
            List<RepositoryNode> repositoryNodes = resetQueryField(queryField);
            for (RepositoryNode repositoryNode : repositoryNodes) {
                QueryNode queryNode = queryNodeMap.computeIfAbsent(repositoryNode, k -> new QueryNode(repositoryNode, new ArrayList<>()));
                queryNode.getQueryFields().add(queryField);
            }
        }

        // 向上遍历
        for (RepositoryNode repositoryNode : queryNodeMap.keySet()) {
            RepositoryNode parent = repositoryNode.getParent();
            while (parent != null) {
                if (!queryNodeMap.containsKey(parent)) {
                    queryNodeMap.put(parent, new QueryNode(parent, new ArrayList<>()));
                }
                parent = parent.getParent();
            }
        }

        // 重新排序
        List<QueryNode> queryNodes = new ArrayList<>(queryNodeMap.values());
        queryNodes.sort(Comparator.comparing(q -> q.getRepositoryNode().getSequence()));
        // 反转顺序
        List<QueryNode> reversedQueryNodes = new ArrayList<>(queryNodes);
        Collections.reverse(reversedQueryNodes);
        // 条件
        ExampleResolver exampleResolver = new ExampleResolver(queryDefinition);

        QueryConfig queryConfig = new QueryConfig();
        queryConfig.setQueryNodes(queryNodes);
        queryConfig.setReversedQueryNodes(reversedQueryNodes);
        queryConfig.setExampleResolver(exampleResolver);
        classQueryConfigMap.put(queryClass, queryConfig);
    }

    private List<RepositoryNode> resetQueryField(QueryFieldDefinition queryFieldDefinition) {
        RepositoryNodeResolver repositoryNodeResolver = repositoryContext.getProperty(RepositoryNodeResolver.class);
        Map<String, RepositoryNode> pathRepositoryNodeMap = repositoryNodeResolver.getPathRepositoryNodeMap();
        Map<Class<?>, List<String>> classPathsMap = repositoryNodeResolver.getClassPathsMap();
        Map<String, List<String>> namePathsMap = repositoryNodeResolver.getNamePathsMap();

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

        List<RepositoryNode> repositoryNodes = new ArrayList<>();
        for (String eachPath : paths) {
            RepositoryNode repositoryNode = pathRepositoryNodeMap.get(eachPath);
            Assert.notNull(repositoryNode, "No merged repository found! path: {}", eachPath);

            RepositoryContext repositoryContext = repositoryNode.getRepository();
            EntityElement entityElement = repositoryContext.getEntityElement();
            Class<?> entityClass = repositoryContext.getEntityClass();
            Assert.isTrue(entityElement.hasField(field),
                    "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                    javaField, entityClass, field);

            repositoryNodes.add(repositoryNode);
        }
        return repositoryNodes;
    }

    public QueryConfig findQueryConfig(Class<?> queryClass) {
        return classQueryConfigMap.get(queryClass);
    }

}
