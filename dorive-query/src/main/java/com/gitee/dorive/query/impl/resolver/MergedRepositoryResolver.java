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
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.CommonRepository;
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
                    repository,
                    getMergedBindersMap(lastAccessPath, repository),
                    abstractContextRepository != null,
                    relativeAccessPath,
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

    private Map<String, List<PropertyBinder>> getMergedBindersMap(String lastAccessPath, CommonRepository repository) {
        BinderResolver binderResolver = repository.getBinderResolver();
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        Map<String, List<PropertyBinder>> mergedBindersMap = new LinkedHashMap<>();
        for (PropertyBinder propertyBinder : propertyBinders) {
            String relativeAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
            List<PropertyBinder> existPropertyBinders = mergedBindersMap.computeIfAbsent(relativeAccessPath, key -> new ArrayList<>(4));
            existPropertyBinders.add(propertyBinder);
        }
        return mergedBindersMap;
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
