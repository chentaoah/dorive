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
package com.gitee.spring.domain.core.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.util.PathUtils;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.EntityElement;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.Map;

public class RepoPropertyResolver {

    private final AbstractContextRepository<?, ?> repository;

    public RepoPropertyResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolvePropertyChainMap() {
        Map<String, PropertyChain> allPropertyChainMap = repository.getPropertyResolver().getAllPropertyChainMap();
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();

        allPropertyChainMap.forEach((accessPath, propertyChain) -> {
            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), lastAccessPath);

            ConfiguredRepository belongRepository = allRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongRepository, "The belong repository cannot be null!");

            Map<String, PropertyChain> repoPropertyChainMap = belongRepository.getPropertyChainMap();
            PropertyChain lastPropertyChain = repoPropertyChainMap.get(lastAccessPath);
            PropertyChain newPropertyChain = new PropertyChain(lastPropertyChain, propertyChain);
            repoPropertyChainMap.put(accessPath, newPropertyChain);
        });

        allRepositoryMap.forEach((accessPath, repository) -> {
            EntityElement entityElement = repository.getEntityElement();
            Map<String, PropertyChain> repoPropertyChainMap = repository.getPropertyChainMap();

            if (repoPropertyChainMap.isEmpty() && entityElement.isCollection()) {
                PropertyResolver propertyResolver = new PropertyResolver();
                propertyResolver.resolveProperties("", entityElement.getGenericEntityClass());
                Map<String, PropertyChain> subPropertyChainMap = propertyResolver.getAllPropertyChainMap();
                repoPropertyChainMap.putAll(subPropertyChainMap);
                repository.setFieldPrefix("/");
            }
        });
    }

}
