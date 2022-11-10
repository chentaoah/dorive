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
package com.gitee.spring.domain.coating.impl.resolver;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.coating.entity.PropertyWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.core.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class RepoDefinitionResolver {

    private AbstractContextRepository<?, ?> repository;

    private Map<String, RepositoryDefinition> repositoryDefinitionMap = new LinkedHashMap<>();

    public RepoDefinitionResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveRepositoryDefinitionMap() {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                "",
                "/",
                false,
                rootRepository,
                rootRepository);
        repositoryDefinitionMap.put("/", repositoryDefinition);
        resolveRepositoryDefinitionMap(new ArrayList<>(), repository);
    }

    private void resolveRepositoryDefinitionMap(List<String> multiAccessPath, AbstractContextRepository<?, ?> repository) {
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        for (ConfiguredRepository subRepository : repository.getSubRepositories()) {
            String accessPath = subRepository.getAccessPath();
            String absoluteAccessPath = prefixAccessPath + accessPath;

            AbstractRepository<Object, Object> abstractRepository = subRepository.getProxyRepository();
            if (abstractRepository instanceof AbstractContextRepository) {
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) abstractRepository;
                ConfiguredRepository rootRepository = abstractContextRepository.getRootRepository();

                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        true,
                        subRepository,
                        rootRepository);
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);

                List<String> newMultiAccessPath = new ArrayList<>(multiAccessPath);
                newMultiAccessPath.add(accessPath);
                resolveRepositoryDefinitionMap(newMultiAccessPath, abstractContextRepository);

            } else {
                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        false,
                        subRepository,
                        subRepository);
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);
            }
        }
    }

    public List<RepositoryWrapper> collectRepositoryWrappers(Map<String, List<PropertyWrapper>> locationPropertyWrappersMap,
                                                             Map<String, PropertyWrapper> fieldPropertyWrapperMap) {
        List<RepositoryWrapper> repositoryWrappers = new ArrayList<>();

        for (RepositoryDefinition repositoryDefinition : repositoryDefinitionMap.values()) {
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            ConfiguredRepository repository = repositoryDefinition.getConfiguredRepository();
            ElementDefinition elementDefinition = repository.getElementDefinition();

            List<PropertyWrapper> propertyWrappers = new ArrayList<>();

            List<PropertyWrapper> locationPropertyWrappers = locationPropertyWrappersMap.get(absoluteAccessPath);
            if (locationPropertyWrappers != null) {
                propertyWrappers.addAll(locationPropertyWrappers);
            }

            for (String fieldName : elementDefinition.getProperties()) {
                PropertyWrapper propertyWrapper = fieldPropertyWrapperMap.get(fieldName);
                if (propertyWrapper != null) {
                    propertyWrappers.add(propertyWrapper);
                }
            }

            if (!propertyWrappers.isEmpty() || repository.isBoundEntity()) {
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper(repositoryDefinition, propertyWrappers);
                repositoryWrappers.add(repositoryWrapper);
            }
        }

        return repositoryWrappers;
    }

}
