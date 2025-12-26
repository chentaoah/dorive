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

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query2.v1.entity.RepositoryNode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class RepositoryNodeResolver {

    private RepositoryContext repositoryContext;
    // all
    private List<RepositoryNode> repositoryNodes = new ArrayList<>();
    // path ==> RepositoryNode
    private Map<String, RepositoryNode> pathRepositoryNodeMap = new LinkedHashMap<>();
    // class ==> paths
    private Map<Class<?>, List<String>> classPathsMap = new LinkedHashMap<>();
    // name ==> paths
    private Map<String, List<String>> namePathsMap = new LinkedHashMap<>();
    // RepositoryContext ==> RepositoryNode
    private Map<RepositoryContext, RepositoryNode> repoRepositoryNodeMap = new LinkedHashMap<>();

    public RepositoryNodeResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
        resolve("", Object.class, "", null, null, null, repositoryContext);
    }

    private void resolve(String absolutePath, Class<?> entityClass, String name,
                         RepositoryNode parent, String lastAccessPath,
                         RepositoryItem lastRepositoryItem, RepositoryContext repositoryContext) {
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        absolutePath = StringUtils.isNotBlank(absolutePath) ? absolutePath : rootRepository.getAccessPath();
        entityClass = entityClass != Object.class ? entityClass : rootRepository.getEntityClass();
        name = StringUtils.isNotBlank(name) ? name : rootRepository.getName();

        RepositoryNode repositoryNode = new RepositoryNode();
        repositoryNode.setParent(parent);
        repositoryNode.setLastAccessPath(lastAccessPath);
        repositoryNode.setLastRepositoryItem(lastRepositoryItem);
        repositoryNode.setAbsolutePath(absolutePath);
        repositoryNode.setRepositoryContext(repositoryContext);
        repositoryNode.setSequence(repositoryNodes.size() + 1);
        repositoryNode.setChildren(new ArrayList<>(8));
        if (parent != null) {
            parent.getChildren().add(repositoryNode);
        }

        repositoryNodes.add(repositoryNode);
        pathRepositoryNodeMap.putIfAbsent(absolutePath, repositoryNode);
        classPathsMap.computeIfAbsent(entityClass, k -> new ArrayList<>(4)).add(absolutePath);
        namePathsMap.computeIfAbsent(name, k -> new ArrayList<>(4)).add(absolutePath);
        repoRepositoryNodeMap.put(repositoryContext, repositoryNode);

        for (RepositoryItem repositoryItem : repositoryContext.getSubRepositories()) {
            RepositoryContext subRepositoryContext = repositoryItem.getRepositoryContext();
            if (subRepositoryContext != null) {
                resolve(getPathPrefix(absolutePath) + repositoryItem.getAccessPath(), repositoryItem.getEntityClass(), repositoryItem.getName(),
                        repositoryNode, repositoryItem.getAccessPath(),
                        repositoryItem, subRepositoryContext);
            }
        }
    }

    private String getPathPrefix(String path) {
        return "/".equals(path) ? "" : path;
    }

    public RepositoryNode findRepositoryNode(RepositoryContext repositoryContext) {
        return repoRepositoryNodeMap.get(repositoryContext);
    }

}
