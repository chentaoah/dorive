/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.impl.core;

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query.v2.entity.core.RepositoryInfo;
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
