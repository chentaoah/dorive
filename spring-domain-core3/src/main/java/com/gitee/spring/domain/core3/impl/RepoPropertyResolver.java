package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.PathUtils;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.Map;

public class RepoPropertyResolver {

    private final AbstractContextRepository<?, ?> repository;

    public RepoPropertyResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveProperties() {
        PropertyResolver propertyResolver = repository.getPropertyResolver();
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();

        Map<String, EntityPropertyChain> allEntityPropertyChainMap = propertyResolver.getProperties();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {

            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), lastAccessPath);

            ConfiguredRepository belongConfiguredRepository = allRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongConfiguredRepository, "The belong repository cannot be null!");

            Map<String, EntityPropertyChain> properties = belongConfiguredRepository.getProperties();
            EntityPropertyChain lastEntityPropertyChain = properties.get(lastAccessPath);
            EntityPropertyChain newEntityPropertyChain = new EntityPropertyChain(lastEntityPropertyChain, entityPropertyChain);
            properties.put(accessPath, newEntityPropertyChain);
        });
    }

}
