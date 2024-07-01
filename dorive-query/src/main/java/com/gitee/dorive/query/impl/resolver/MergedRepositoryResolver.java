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

import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueRouteBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MergedRepositoryResolver {

    private AbstractContextRepository<?, ?> repository;
    // absoluteAccessPath ==> MergedRepository
    private Map<String, MergedRepository> mergedRepositoryMap = new LinkedHashMap<>();
    // name ==> MergedRepository
    private Map<String, MergedRepository> nameMergedRepositoryMap = new LinkedHashMap<>();

    public MergedRepositoryResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolve() {
        for (CommonRepository repository : repository.getRepositoryMap().values()) {
            String accessPath = repository.getAccessPath();
            BinderResolver binderResolver = repository.getBinderResolver();

            String relativeAccessPath = accessPath;
            CommonRepository executedRepository = repository;
            AbstractRepository<Object, Object> abstractRepository = repository.getProxyRepository();
            AbstractQueryRepository<?, ?> abstractQueryRepository = null;
            if (abstractRepository instanceof AbstractQueryRepository) {
                abstractQueryRepository = (AbstractQueryRepository<?, ?>) abstractRepository;
                relativeAccessPath = relativeAccessPath + "/";
                executedRepository = abstractQueryRepository.getRootRepository();
            }

            MergedRepository mergedRepository = new MergedRepository();
            mergedRepository.setLastAccessPath("");
            mergedRepository.setAbsoluteAccessPath(accessPath);
            mergedRepository.setRelativeAccessPath(relativeAccessPath);
            mergedRepository.setMerged(abstractQueryRepository != null);
            mergedRepository.setDefinedRepository(repository);
            mergedRepository.setRelativeStrongBindersMap(new LinkedHashMap<>(binderResolver.getMergedStrongBindersMap()));
            mergedRepository.setRelativeValueRouteBindersMap(new LinkedHashMap<>(binderResolver.getMergedValueRouteBindersMap()));
            mergedRepository.setExecutedRepository(executedRepository);
            mergedRepository.setOrder(mergedRepositoryMap.size() + 1);
            addMergedRepository(mergedRepository);

            if (abstractQueryRepository != null) {
                mergeRepository(accessPath, abstractQueryRepository);
            }
        }
    }

    private void addMergedRepository(MergedRepository mergedRepository) {
        String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
        mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);
        String name = mergedRepository.getName();
        if (StringUtils.isNotBlank(name)) {
            nameMergedRepositoryMap.putIfAbsent(name, mergedRepository);
        }
    }

    private void mergeRepository(String accessPath, AbstractQueryRepository<?, ?> repository) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        for (MergedRepository mergedRepository : mergedRepositoryResolver.getMergedRepositoryMap().values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            if ("/".equals(absoluteAccessPath)) {
                continue;
            }
            MergedRepository newMergedRepository = new MergedRepository();
            newMergedRepository.setLastAccessPath(accessPath + mergedRepository.getLastAccessPath());
            newMergedRepository.setAbsoluteAccessPath(accessPath + mergedRepository.getAbsoluteAccessPath());
            newMergedRepository.setRelativeAccessPath(accessPath + mergedRepository.getRelativeAccessPath());
            newMergedRepository.setMerged(mergedRepository.isMerged());
            newMergedRepository.setDefinedRepository(mergedRepository.getDefinedRepository());

            Map<String, List<StrongBinder>> relativeStrongBindersMap = mergedRepository.getRelativeStrongBindersMap();
            Map<String, List<StrongBinder>> newRelativeStrongBindersMap = new LinkedHashMap<>(relativeStrongBindersMap.size() * 4 / 3 + 1);
            relativeStrongBindersMap.forEach((k, v) -> newRelativeStrongBindersMap.put(accessPath + k, v));
            newMergedRepository.setRelativeStrongBindersMap(newRelativeStrongBindersMap);

            Map<String, List<ValueRouteBinder>> relativeValueRouteBindersMap = mergedRepository.getRelativeValueRouteBindersMap();
            Map<String, List<ValueRouteBinder>> newRelativeValueRouteBindersMap = new LinkedHashMap<>(relativeValueRouteBindersMap.size() * 4 / 3 + 1);
            relativeValueRouteBindersMap.forEach((k, v) -> newRelativeValueRouteBindersMap.put(accessPath + k, v));
            newMergedRepository.setRelativeValueRouteBindersMap(newRelativeValueRouteBindersMap);

            newMergedRepository.setExecutedRepository(mergedRepository.getExecutedRepository());
            newMergedRepository.setOrder(mergedRepositoryMap.size() + 1);
            addMergedRepository(newMergedRepository);
        }
    }

}
