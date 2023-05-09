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

package com.gitee.dorive.coating.impl.resolver;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MergedRepositoryResolver {

    private AbstractContextRepository<?, ?> repository;
    private Map<String, MergedRepository> mergedRepositoryMap = new LinkedHashMap<>();

    public MergedRepositoryResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        resolve();
    }

    public void resolve() {
        CommonRepository rootRepository = repository.getRootRepository();
        mergedRepositoryMap.put("/", new MergedRepository(
                "",
                "/",
                false,
                "/",
                rootRepository,
                getMergedBindersMap("", rootRepository),
                rootRepository,
                1));
        resolve(new ArrayList<>(), repository);
    }

    private void resolve(List<String> multiAccessPath, AbstractContextRepository<?, ?> lastRepository) {
        String lastAccessPath = StrUtil.join("", multiAccessPath);

        for (CommonRepository repository : lastRepository.getSubRepositories()) {
            String accessPath = repository.getAccessPath();
            String absoluteAccessPath = lastAccessPath + accessPath;
            AbstractRepository<Object, Object> abstractRepository = repository.getProxyRepository();

            if (abstractRepository instanceof AbstractContextRepository) {
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) abstractRepository;
                CommonRepository rootRepository = abstractContextRepository.getRootRepository();

                MergedRepository mergedRepository = new MergedRepository(
                        lastAccessPath,
                        absoluteAccessPath,
                        true,
                        absoluteAccessPath + "/",
                        repository,
                        getMergedBindersMap(lastAccessPath, repository),
                        rootRepository,
                        mergedRepositoryMap.size() + 1);
                mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);

                List<String> newMultiAccessPath = new ArrayList<>(multiAccessPath);
                newMultiAccessPath.add(accessPath);
                resolve(newMultiAccessPath, abstractContextRepository);

            } else {
                MergedRepository mergedRepository = new MergedRepository(
                        lastAccessPath,
                        absoluteAccessPath,
                        false,
                        absoluteAccessPath,
                        repository,
                        getMergedBindersMap(lastAccessPath, repository),
                        repository,
                        mergedRepositoryMap.size() + 1);
                mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);
            }
        }
    }

    private Map<String, List<PropertyBinder>> getMergedBindersMap(String lastAccessPath, CommonRepository repository) {
        BinderResolver binderResolver = repository.getBinderResolver();
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        Map<String, List<PropertyBinder>> mergedBindersMap = new LinkedHashMap<>();
        for (PropertyBinder propertyBinder : propertyBinders) {
            String relativeAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
            List<PropertyBinder> existPropertyBinders = mergedBindersMap.computeIfAbsent(relativeAccessPath, key -> new ArrayList<>());
            existPropertyBinders.add(propertyBinder);
        }
        return mergedBindersMap;
    }

}
