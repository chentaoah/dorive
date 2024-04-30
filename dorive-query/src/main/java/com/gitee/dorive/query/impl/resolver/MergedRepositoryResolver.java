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

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.impl.binder.BoundBinder;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueRouteBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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
        resolve(new ArrayList<>(), false, repository);
    }

    private void resolve(List<String> multiAccessPath, boolean ignoreRoot, AbstractContextRepository<?, ?> contextRepository) {
        String lastAccessPath = StrUtil.join("", multiAccessPath);

        Collection<CommonRepository> repositories = contextRepository.getRepositoryMap().values();
        for (CommonRepository repository : repositories) {
            String accessPath = repository.getAccessPath();
            boolean isRoot = repository.isRoot();
            if (ignoreRoot && isRoot) {
                continue;
            }

            String absoluteAccessPath = lastAccessPath + accessPath;
            String relativeAccessPath = absoluteAccessPath;
            CommonRepository executedRepository = repository;

            AbstractRepository<Object, Object> abstractRepository = repository.getProxyRepository();
            AbstractContextRepository<?, ?> abstractContextRepository = null;
            if (abstractRepository instanceof AbstractContextRepository) {
                abstractContextRepository = (AbstractContextRepository<?, ?>) abstractRepository;
                relativeAccessPath = relativeAccessPath + "/";
                executedRepository = abstractContextRepository.getRootRepository();
            }

            MergedRepository mergedRepository = new MergedRepository(
                    lastAccessPath,
                    absoluteAccessPath,
                    relativeAccessPath,
                    abstractContextRepository != null,
                    repository,
                    getRelativeValueRouteBindersMap(lastAccessPath, repository),
                    getRelativeStrongBindersMap(lastAccessPath, repository),
                    executedRepository,
                    mergedRepositoryMap.size() + 1);
            addMergedRepository(mergedRepository);

            if (abstractContextRepository != null) {
                List<String> newMultiAccessPath = new ArrayList<>(multiAccessPath);
                newMultiAccessPath.add(accessPath);
                resolve(newMultiAccessPath, true, abstractContextRepository);
            }
        }
    }

    private Map<String, List<ValueRouteBinder>> getRelativeValueRouteBindersMap(String lastAccessPath, CommonRepository repository) {
        BinderResolver binderResolver = repository.getBinderResolver();
        List<ValueRouteBinder> valueRouteBinders = binderResolver.getValueRouteBinders();
        Map<String, List<ValueRouteBinder>> relativeValueRouteBindersMap = new LinkedHashMap<>();
        for (ValueRouteBinder valueRouteBinder : valueRouteBinders) {
            String relativeAccessPath = lastAccessPath + valueRouteBinder.getBelongAccessPath();
            List<ValueRouteBinder> existBinders = relativeValueRouteBindersMap.computeIfAbsent(relativeAccessPath, key -> new ArrayList<>(4));
            existBinders.add(valueRouteBinder);
        }
        return relativeValueRouteBindersMap;
    }

    private Map<String, List<StrongBinder>> getRelativeStrongBindersMap(String lastAccessPath, CommonRepository repository) {
        BinderResolver binderResolver = repository.getBinderResolver();
        List<StrongBinder> strongBinders = binderResolver.getStrongBinders();
        Map<String, List<StrongBinder>> relativeStrongBindersMap = new LinkedHashMap<>();
        for (StrongBinder strongBinder : strongBinders) {
            BoundBinder boundBinder = strongBinder.getBoundBinder();
            String relativeAccessPath = lastAccessPath + boundBinder.getBelongAccessPath();
            List<StrongBinder> existBinders = relativeStrongBindersMap.computeIfAbsent(relativeAccessPath, key -> new ArrayList<>(4));
            existBinders.add(strongBinder);
        }
        return relativeStrongBindersMap;
    }

    private void addMergedRepository(MergedRepository mergedRepository) {
        String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
        mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);
        String name = mergedRepository.getName();
        if (StringUtils.isNotBlank(name)) {
            nameMergedRepositoryMap.putIfAbsent(name, mergedRepository);
        }
    }

}
