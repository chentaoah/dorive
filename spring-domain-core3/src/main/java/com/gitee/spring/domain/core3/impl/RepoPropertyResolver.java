package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.PathUtils;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.BindRepository;

import java.util.Map;

public class RepoPropertyResolver {

    private final AbstractContextRepository<?, ?> repository;

    public RepoPropertyResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveProperties() {
        PropertyResolver propertyResolver = repository.getPropertyResolver();
        Map<String, BindRepository> allRepositoryMap = repository.getAllRepositoryMap();

        Map<String, EntityPropertyChain> allEntityPropertyChainMap = propertyResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {

            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), lastAccessPath);

            BindRepository belongBindRepository = allRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongBindRepository, "The belong repository cannot be null!");

            Map<String, EntityPropertyChain> properties = belongBindRepository.getProperties();
            EntityPropertyChain lastEntityPropertyChain = properties.get(lastAccessPath);
            EntityPropertyChain newEntityPropertyChain = new EntityPropertyChain(lastEntityPropertyChain, entityPropertyChain);
            properties.put(accessPath, newEntityPropertyChain);
        });
    }

}
