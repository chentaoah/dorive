package com.gitee.spring.domain.core3.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core3.util.PathUtils;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

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
            ElementDefinition elementDefinition = repository.getElementDefinition();
            Map<String, PropertyChain> repoPropertyChainMap = repository.getPropertyChainMap();

            if (repoPropertyChainMap.isEmpty() && elementDefinition.isCollection()) {
                PropertyResolver propertyResolver = new PropertyResolver();
                propertyResolver.resolveProperties("", elementDefinition.getGenericEntityClass());
                Map<String, PropertyChain> subPropertyChainMap = propertyResolver.getAllPropertyChainMap();
                repoPropertyChainMap.putAll(subPropertyChainMap);
                repository.setFieldPrefix("/");
            }
        });
    }

}
