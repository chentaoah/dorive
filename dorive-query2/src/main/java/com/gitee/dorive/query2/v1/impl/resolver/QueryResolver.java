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

package com.gitee.dorive.query2.v1.impl.resolver;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import com.gitee.dorive.base.v1.common.def.QueryFieldDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.common.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query2.v1.entity.QueryConfig;
import com.gitee.dorive.query2.v1.entity.RepositoryNode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class QueryResolver {

    private final RepositoryContext repositoryContext;
    private Map<Class<?>, QueryConfig> classQueryConfigMap = new ConcurrentHashMap<>();

    public QueryResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
    }

    public void resolve() {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        Class<?>[] queries = repositoryDef.getQueries();
        for (Class<?> queryClass : queries) {
            resolveQueryClass(queryClass);
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        com.gitee.dorive.base.v1.aggregate.api.QueryResolver queryResolver = SpringUtil.getBean(com.gitee.dorive.base.v1.aggregate.api.QueryResolver.class);
        QueryDefinition queryDefinition = queryResolver.resolve(queryClass);

        ExampleResolver exampleResolver = new ExampleResolver(queryDefinition);

        List<RepositoryNode> repositoryNodes = new ArrayList<>();
        for (QueryFieldDefinition queryFieldDefinition : queryDefinition.getQueryFieldDefinitions()) {
            for (RepositoryNode repositoryNode : resetQueryField(queryFieldDefinition)) {
                if (!repositoryNodes.contains(repositoryNode)) {
                    repositoryNodes.add(repositoryNode);
                }
            }
        }
        // 重新排序
        repositoryNodes.sort(Comparator.comparing(RepositoryNode::getSequence));
        // 反转顺序
        List<RepositoryNode> reversedRepositoryNodes = new ArrayList<>(repositoryNodes);
        Collections.reverse(reversedRepositoryNodes);

        QueryConfig queryConfig = new QueryConfig();
        queryConfig.setRepositoryContext(repositoryContext);
        queryConfig.setExampleResolver(exampleResolver);
        queryConfig.setRepositoryNodes(repositoryNodes);
        queryConfig.setReversedRepositoryNodes(reversedRepositoryNodes);
        classQueryConfigMap.put(queryClass, queryConfig);
    }

    private List<RepositoryNode> resetQueryField(QueryFieldDefinition queryFieldDefinition) {
        RepositoryResolver repositoryResolver = repositoryContext.getProperty(RepositoryResolver.class);
        Map<String, RepositoryNode> pathRepositoryNodeMap = repositoryResolver.getPathRepositoryNodeMap();
        Map<Class<?>, List<String>> classPathsMap = repositoryResolver.getClassPathsMap();
        Map<String, List<String>> namePathsMap = repositoryResolver.getNamePathsMap();

        QueryFieldDef queryFieldDef = queryFieldDefinition.getQueryFieldDef();
        String path = queryFieldDef.getPath();
        Class<?> entity = queryFieldDef.getEntity();
        String name = queryFieldDef.getName();
        String field = queryFieldDef.getField();

        // 路径 > 类型 > 名称
        List<String> paths = Collections.emptyList();
        if (StringUtils.isNotBlank(path)) {
            paths = Collections.singletonList(path);

        } else if (entity != Object.class) {
            paths = classPathsMap.get(entity);
            Assert.notEmpty(paths, "No merged repository found! entity: {}", entity.getName());

        } else if (StringUtils.isNotBlank(name)) {
            paths = namePathsMap.get(name);
            Assert.notEmpty(paths, "No merged repository found! name: {}", name);
        }

        List<RepositoryNode> repositoryNodes = new ArrayList<>();
        for (String eachPath : paths) {
            RepositoryNode repositoryNode = pathRepositoryNodeMap.get(eachPath);
            Assert.notNull(repositoryNode, "No merged repository found! path: {}", eachPath);

            RepositoryContext repositoryContext = repositoryNode.getRepository();
            EntityElement entityElement = repositoryContext.getEntityElement();
            Assert.isTrue(entityElement.hasField(field), "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                    queryFieldDefinition.getField(), repositoryContext.getEntityClass(), field);

            repositoryNodes.add(repositoryNode);
        }
        return repositoryNodes;
    }

}
