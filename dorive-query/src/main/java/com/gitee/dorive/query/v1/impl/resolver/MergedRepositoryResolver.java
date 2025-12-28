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

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.query.v1.entity.MergedRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public class MergedRepositoryResolver {

    private RepositoryContext repository;
    // absoluteAccessPath ==> MergedRepository
    private Map<String, MergedRepository> mergedRepositoryMap = new LinkedHashMap<>();
    // class ==> MergedRepository
    private Map<Class<?>, MergedRepository> classMergedRepositoryMap = new LinkedHashMap<>();
    // name ==> MergedRepository
    private Map<String, MergedRepository> nameMergedRepositoryMap = new LinkedHashMap<>();

    public MergedRepositoryResolver(RepositoryContext repository) {
        this.repository = repository;
    }

    public void resolve() {
        for (RepositoryItem repositoryItem : repository.getRepositoryMap().values()) {
            String accessPath = repositoryItem.getAccessPath();
            BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();

            RepositoryItem executedRepository = repositoryItem;
            AbstractRepository<Object, Object> abstractRepository = repositoryItem.getProxyRepository();
            RepositoryContext abstractQueryRepository = null;
            if (abstractRepository instanceof RepositoryContext) {
                abstractQueryRepository = (RepositoryContext) abstractRepository;
                executedRepository = abstractQueryRepository.getRootRepository();
            }

            MergedRepository mergedRepository = new MergedRepository();
            mergedRepository.setLastAccessPath("");
            mergedRepository.setAbsoluteAccessPath(accessPath);
            mergedRepository.setDefinedRepository(repositoryItem);
            mergedRepository.setMergedStrongBindersMap(new LinkedHashMap<>(binderExecutor.getMergedStrongBindersMap()));
            mergedRepository.setMergedValueRouteBindersMap(new LinkedHashMap<>(binderExecutor.getMergedValueRouteBindersMap()));

            Set<String> boundAccessPaths = new LinkedHashSet<>(8);
            boundAccessPaths.addAll(mergedRepository.getMergedStrongBindersMap().keySet());
            boundAccessPaths.addAll(mergedRepository.getMergedValueRouteBindersMap().keySet());
            mergedRepository.setBoundAccessPaths(boundAccessPaths);

            mergedRepository.setExecutedRepository(executedRepository);
            addMergedRepository(mergedRepository);

            // 合并内部仓储
            if (abstractQueryRepository != null) {
                mergeRepository(accessPath, abstractQueryRepository);
            }
        }
    }

    private void addMergedRepository(MergedRepository mergedRepository) {
        RepositoryItem executedRepository = mergedRepository.getExecutedRepository();
        mergedRepository.setDefaultRepository((DefaultRepository) executedRepository.getProxyRepository());
        mergedRepository.setSequence(mergedRepositoryMap.size() + 1);
        mergedRepository.setAlias("t" + mergedRepository.getSequence());

        String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
        mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);

        Class<?> entityClass = mergedRepository.getEntityClass();
        classMergedRepositoryMap.putIfAbsent(entityClass, mergedRepository);

        String name = mergedRepository.getName();
        if (StringUtils.isNotBlank(name)) {
            nameMergedRepositoryMap.putIfAbsent(name, mergedRepository);
        }
    }

    private void mergeRepository(String accessPath, RepositoryContext repository) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getProperty(MergedRepositoryResolver.class);
        for (MergedRepository mergedRepository : mergedRepositoryResolver.getMergedRepositoryMap().values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            // 筛除根节点
            if ("/".equals(absoluteAccessPath)) {
                continue;
            }
            MergedRepository newMergedRepository = new MergedRepository();
            newMergedRepository.setLastAccessPath(accessPath + mergedRepository.getLastAccessPath());
            newMergedRepository.setAbsoluteAccessPath(accessPath + mergedRepository.getAbsoluteAccessPath());
            newMergedRepository.setDefinedRepository(mergedRepository.getDefinedRepository());

            Map<String, List<Binder>> mergedStrongBindersMap = mergedRepository.getMergedStrongBindersMap();
            Map<String, List<Binder>> newMergedStrongBindersMap = new LinkedHashMap<>(mergedStrongBindersMap.size() * 4 / 3 + 1);
            mergedStrongBindersMap.forEach((k, v) -> newMergedStrongBindersMap.put(mergeAccessPath(accessPath, k), v));
            newMergedRepository.setMergedStrongBindersMap(newMergedStrongBindersMap);

            Map<String, List<Binder>> mergedValueRouteBindersMap = mergedRepository.getMergedValueRouteBindersMap();
            Map<String, List<Binder>> newMergedValueRouteBindersMap = new LinkedHashMap<>(mergedValueRouteBindersMap.size() * 4 / 3 + 1);
            mergedValueRouteBindersMap.forEach((k, v) -> newMergedValueRouteBindersMap.put(mergeAccessPath(accessPath, k), v));
            newMergedRepository.setMergedValueRouteBindersMap(newMergedValueRouteBindersMap);

            Set<String> boundAccessPaths = mergedRepository.getBoundAccessPaths();
            Set<String> newBoundAccessPaths = new LinkedHashSet<>(boundAccessPaths.size() * 4 / 3 + 1);
            boundAccessPaths.forEach(k -> newBoundAccessPaths.add(mergeAccessPath(accessPath, k)));
            newMergedRepository.setBoundAccessPaths(newBoundAccessPaths);

            newMergedRepository.setExecutedRepository(mergedRepository.getExecutedRepository());
            addMergedRepository(newMergedRepository);
        }
    }

    private String mergeAccessPath(String lastAccessPath, String accessPath) {
        return "/".equals(accessPath) ? lastAccessPath : lastAccessPath + accessPath;
    }

}
