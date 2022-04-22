package com.gitee.spring.domain.core.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.EntityPropertyLocation;

import java.util.*;

public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected List<ConfiguredRepository> delegateConfiguredRepositories = new ArrayList<>();

    @Override
    protected EntityPropertyChain newEntityPropertyChain(String accessPath, Class<?> lastEntityClass, Class<?> entityClass, String fieldName) {
        EntityPropertyChain entityPropertyChain = super.newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
        if (!fieldEntityPropertyChainMap.containsKey(fieldName)) {
            fieldEntityPropertyChainMap.put(fieldName, entityPropertyChain);
        }
        return entityPropertyChain;
    }

    @Override
    protected ConfiguredRepository newConfiguredRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                           EntityMapper entityMapper, EntityAssembler entityAssembler,
                                                           AbstractRepository<Object, Object> repository) {
        ConfiguredRepository configuredRepository = super.newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
        if (repository instanceof AbstractDelegateRepository) {
            delegateConfiguredRepositories.add(configuredRepository);
        }
        return configuredRepository;
    }

    protected List<EntityPropertyLocation> collectEntityPropertyLocations(Set<String> fieldNames) {
        List<EntityPropertyLocation> entityPropertyLocations = new ArrayList<>();
        collectEntityPropertyLocations(new ArrayList<>(), null, this, fieldNames, entityPropertyLocations);
        return entityPropertyLocations;
    }

    protected void collectEntityPropertyLocations(List<String> multiAccessPath, ConfiguredRepository parentConfiguredRepository,
                                                  AbstractDelegateRepository<?, ?> abstractDelegateRepository, Set<String> fieldNames,
                                                  List<EntityPropertyLocation> entityPropertyLocations) {
        for (EntityPropertyChain entityPropertyChain : entityPropertyChainMap.values()) {
            if (fieldNames.contains(entityPropertyChain.getFieldName()) || entityPropertyChain.isBoundProperty()) {
                String prefixAccessPath = StrUtil.join("", multiAccessPath);

                ConfiguredRepository belongConfiguredRepository = findBelongConfiguredRepository(entityPropertyChain);
                EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

                String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";

                EntityPropertyLocation entityPropertyLocation = new EntityPropertyLocation(multiAccessPath, prefixAccessPath, forwardParent, parentAccessPath,
                        parentConfiguredRepository, abstractDelegateRepository, entityPropertyChain, belongConfiguredRepository);
                entityPropertyLocations.add(entityPropertyLocation);
            }
        }
        for (ConfiguredRepository configuredRepository : delegateConfiguredRepositories) {
            multiAccessPath = new ArrayList<>(multiAccessPath);
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            multiAccessPath.add(entityDefinition.getAccessPath());
            AbstractDelegateRepository<?, ?> delegateRepository = (AbstractDelegateRepository<?, ?>) configuredRepository.getRepository();
            collectEntityPropertyLocations(multiAccessPath, configuredRepository, delegateRepository, fieldNames, entityPropertyLocations);
        }
    }

    protected ConfiguredRepository findBelongConfiguredRepository(EntityPropertyChain entityPropertyChain) {
        String belongAccessPath = getBelongAccessPath(entityPropertyChain.getAccessPath());
        return configuredRepositoryMap.get(belongAccessPath);
    }

}
