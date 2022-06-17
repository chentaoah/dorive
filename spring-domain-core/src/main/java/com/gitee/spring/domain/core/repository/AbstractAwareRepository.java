package com.gitee.spring.domain.core.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.RepositoryLocation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAwareRepository<E, PK> extends AbstractDelegateRepository<E, PK> {

    protected Map<String, RepositoryLocation> repositoryLocationMap = new LinkedHashMap<>();
    protected List<RepositoryLocation> repositoryLocations = new ArrayList<>();
    protected List<RepositoryLocation> reversedRepositoryLocations = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        resolveRepositoryLocationMap(new ArrayList<>(), null, this);
        repositoryLocations.addAll(repositoryLocationMap.values());
        reversedRepositoryLocations.addAll(repositoryLocationMap.values());
        Collections.reverse(reversedRepositoryLocations);
    }

    protected void resolveRepositoryLocationMap(List<String> multiAccessPath,
                                                ConfiguredRepository parentConfiguredRepository,
                                                AbstractDelegateRepository<?, ?> abstractDelegateRepository) {

        List<String> finalMultiAccessPath = multiAccessPath;
        String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        Map<String, ConfiguredRepository> allConfiguredRepositoryMap = abstractDelegateRepository.getAllConfiguredRepositoryMap();
        allConfiguredRepositoryMap.forEach((accessPath, configuredRepository) -> {

            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
            boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

            RepositoryLocation repositoryLocation = new RepositoryLocation(
                    finalMultiAccessPath,
                    parentAccessPath,
                    prefixAccessPath,
                    absoluteAccessPath,
                    forwardParent,
                    parentConfiguredRepository,
                    abstractDelegateRepository,
                    configuredRepository);
            repositoryLocationMap.put(absoluteAccessPath, repositoryLocation);
        });

        for (ConfiguredRepository configuredRepository : delegateConfiguredRepositories) {
            multiAccessPath = new ArrayList<>(multiAccessPath);
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            multiAccessPath.add(entityDefinition.getAccessPath());
            AbstractDelegateRepository<?, ?> delegateRepository = (AbstractDelegateRepository<?, ?>) configuredRepository.getRepository();
            resolveRepositoryLocationMap(multiAccessPath, configuredRepository, delegateRepository);
        }
    }

    protected AbstractAwareRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return this;
    }

}
