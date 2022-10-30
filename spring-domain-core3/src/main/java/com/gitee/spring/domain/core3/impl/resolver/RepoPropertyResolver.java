package com.gitee.spring.domain.core3.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.common.util.PathUtils;
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

    public void resolvePropertyChains() {
        Map<String, PropertyChain> propertyChains = repository.getPropertyResolver().getPropertyChains();
        Map<String, ConfiguredRepository> accessPathRepositoryMap = repository.getAccessPathRepositoryMap();

        propertyChains.forEach((accessPath, propertyChain) -> {
            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            String belongAccessPath = PathUtils.getBelongPath(accessPathRepositoryMap.keySet(), lastAccessPath);

            ConfiguredRepository belongRepository = accessPathRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongRepository, "The belong repository cannot be null!");

            Map<String, PropertyChain> repoPropertyChains = belongRepository.getPropertyChains();
            PropertyChain lastPropertyChain = repoPropertyChains.get(lastAccessPath);
            PropertyChain newPropertyChain = new PropertyChain(lastPropertyChain, propertyChain);
            repoPropertyChains.put(accessPath, newPropertyChain);
        });

        accessPathRepositoryMap.forEach((accessPath, repository) -> {
            ElementDefinition elementDefinition = repository.getElementDefinition();
            Map<String, PropertyChain> repoPropertyChains = repository.getPropertyChains();

            if (repoPropertyChains.isEmpty() && elementDefinition.isCollection()) {
                PropertyResolver propertyResolver = new PropertyResolver();
                propertyResolver.resolveProperties("", elementDefinition.getGenericEntityClass());
                Map<String, PropertyChain> subPropertyChains = propertyResolver.getPropertyChains();
                repoPropertyChains.putAll(subPropertyChains);
                repository.setFieldPrefix("/");
            }
        });
    }

}
