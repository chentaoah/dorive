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

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query.v2.entity.RepositoryInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class RepositoryInfoResolver {

    private RepositoryContext repositoryContext;
    // all
    private List<RepositoryInfo> repositoryInfos = new ArrayList<>();
    // path ==> RepositoryInfo
    private Map<String, RepositoryInfo> pathRepositoryInfoMap = new LinkedHashMap<>();
    // class ==> paths
    private Map<Class<?>, List<String>> classPathsMap = new LinkedHashMap<>();
    // name ==> paths
    private Map<String, List<String>> namePathsMap = new LinkedHashMap<>();
    // RepositoryContext ==> RepositoryInfo
    private Map<RepositoryContext, RepositoryInfo> repoRepositoryInfoMap = new LinkedHashMap<>();

    public RepositoryInfoResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
        resolve("", Object.class, "", null, null, null, repositoryContext);
    }

    private void resolve(String absolutePath, Class<?> entityClass, String name,
                         RepositoryInfo parent, String lastAccessPath,
                         RepositoryItem lastRepositoryItem, RepositoryContext repositoryContext) {
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        absolutePath = StringUtils.isNotBlank(absolutePath) ? absolutePath : rootRepository.getAccessPath();
        entityClass = entityClass != Object.class ? entityClass : rootRepository.getEntityClass();
        name = StringUtils.isNotBlank(name) ? name : rootRepository.getName();

        RepositoryInfo repositoryInfo = new RepositoryInfo();
        repositoryInfo.setParent(parent);
        repositoryInfo.setLastAccessPath(lastAccessPath);
        repositoryInfo.setLastRepositoryItem(lastRepositoryItem);
        repositoryInfo.setAbsolutePath(absolutePath);
        repositoryInfo.setRepositoryContext(repositoryContext);
        repositoryInfo.setSequence(repositoryInfos.size() + 1);
        repositoryInfo.setChildren(new ArrayList<>(8));
        if (parent != null) {
            parent.getChildren().add(repositoryInfo);
        }

        repositoryInfos.add(repositoryInfo);
        pathRepositoryInfoMap.putIfAbsent(absolutePath, repositoryInfo);
        classPathsMap.computeIfAbsent(entityClass, k -> new ArrayList<>(4)).add(absolutePath);
        namePathsMap.computeIfAbsent(name, k -> new ArrayList<>(4)).add(absolutePath);
        repoRepositoryInfoMap.put(repositoryContext, repositoryInfo);

        for (RepositoryItem repositoryItem : repositoryContext.getSubRepositories()) {
            RepositoryContext subRepositoryContext = repositoryItem.getRepositoryContext();
            if (subRepositoryContext != null) {
                resolve(getPathPrefix(absolutePath) + repositoryItem.getAccessPath(), repositoryItem.getEntityClass(), repositoryItem.getName(),
                        repositoryInfo, repositoryItem.getAccessPath(),
                        repositoryItem, subRepositoryContext);
            }
        }
    }

    private String getPathPrefix(String path) {
        return "/".equals(path) ? "" : path;
    }

    public RepositoryInfo findRepositoryInfo(RepositoryContext repositoryContext) {
        return repoRepositoryInfoMap.get(repositoryContext);
    }

}
