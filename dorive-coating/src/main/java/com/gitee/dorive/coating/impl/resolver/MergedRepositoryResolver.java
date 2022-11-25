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
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.ConfiguredRepository;
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
    }

    public void resolveMergedRepositoryMap() {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        MergedRepository mergedRepository = new MergedRepository(
                "",
                "/",
                false,
                rootRepository,
                rootRepository);
        mergedRepositoryMap.put("/", mergedRepository);
        resolveMergedRepositoryMap(new ArrayList<>(), repository);
    }

    private void resolveMergedRepositoryMap(List<String> multiAccessPath, AbstractContextRepository<?, ?> lastRepository) {
        String lastAccessPath = StrUtil.join("", multiAccessPath);

        for (ConfiguredRepository repository : lastRepository.getSubRepositories()) {
            String accessPath = repository.getAccessPath();
            String absoluteAccessPath = lastAccessPath + accessPath;
            AbstractRepository<Object, Object> abstractRepository = repository.getProxyRepository();

            if (abstractRepository instanceof AbstractContextRepository) {
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) abstractRepository;
                ConfiguredRepository rootRepository = abstractContextRepository.getRootRepository();

                MergedRepository mergedRepository = new MergedRepository(
                        lastAccessPath,
                        absoluteAccessPath,
                        true,
                        repository,
                        rootRepository);
                mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);

                List<String> newMultiAccessPath = new ArrayList<>(multiAccessPath);
                newMultiAccessPath.add(accessPath);
                resolveMergedRepositoryMap(newMultiAccessPath, abstractContextRepository);

            } else {
                MergedRepository mergedRepository = new MergedRepository(
                        lastAccessPath,
                        absoluteAccessPath,
                        false,
                        repository,
                        repository);
                mergedRepositoryMap.put(absoluteAccessPath, mergedRepository);
            }
        }
    }

}
