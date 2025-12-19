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

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.query2.v1.entity.RepositoryNode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class RepositoryResolver {

    private List<RepositoryNode> repositoryNodes = new ArrayList<>();
    // path ==> RepositoryNode
    private Map<String, RepositoryNode> pathRepositoryNodeMap = new LinkedHashMap<>();
    // class ==> paths
    private Map<Class<?>, List<String>> classPathsMap = new LinkedHashMap<>();
    // name ==> paths
    private Map<String, List<String>> namePathsMap = new LinkedHashMap<>();

    public void resolve(RepositoryContext repositoryContext) {
        doResolve("", Object.class, "", null, null, repositoryContext);
    }

    private void doResolve(String lastPath, Class<?> lastEntityClass, String lastName,
                           RepositoryNode lastRepositoryNode, String lastAccessPath,
                           RepositoryContext repositoryContext) {
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        String path = StringUtils.isNotBlank(lastPath) ? lastPath : rootRepository.getAccessPath();
        Class<?> entityClass = lastEntityClass != Object.class ? lastEntityClass : rootRepository.getEntityClass();
        String name = StringUtils.isNotBlank(lastName) ? lastName : rootRepository.getName();

        RepositoryNode repositoryNode = new RepositoryNode();
        repositoryNode.setParent(lastRepositoryNode);
        repositoryNode.setLastAccessPath(lastAccessPath);
        repositoryNode.setPath(path);
        repositoryNode.setSequence(repositoryNodes.size() + 1);
        repositoryNode.setRepository(repositoryContext);
        repositoryNode.setChildren(new ArrayList<>(8));
        if (lastRepositoryNode != null) {
            lastRepositoryNode.getChildren().add(repositoryNode);
        }

        repositoryNodes.add(repositoryNode);
        pathRepositoryNodeMap.putIfAbsent(path, repositoryNode);
        classPathsMap.computeIfAbsent(entityClass, k -> new ArrayList<>(4)).add(path);
        namePathsMap.computeIfAbsent(name, k -> new ArrayList<>(4)).add(path);

        for (RepositoryItem repositoryItem : repositoryContext.getSubRepositories()) {
            AbstractRepository<Object, Object> abstractRepository = repositoryItem.getProxyRepository();
            if (abstractRepository instanceof RepositoryContext) {
                doResolve(lastPath + repositoryItem.getAccessPath(), repositoryItem.getEntityClass(), repositoryItem.getName(),
                        repositoryNode, repositoryItem.getAccessPath(),
                        (RepositoryContext) abstractRepository);
            }
        }
    }

}
