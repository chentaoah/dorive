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

    protected EntityPropertyLocation findEntityPropertyLocation(String fieldName) {
        return doFindEntityPropertyLocation(new ArrayList<>(), null, this, fieldName);
    }

    protected EntityPropertyLocation doFindEntityPropertyLocation(List<String> multiAccessPath, ConfiguredRepository parentConfiguredRepository,
                                                                  AbstractDelegateRepository<?, ?> abstractDelegateRepository, String fieldName) {
        Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = abstractDelegateRepository.fieldEntityPropertyChainMap;
        EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
        if (entityPropertyChain == null) {
            for (ConfiguredRepository configuredRepository : delegateConfiguredRepositories) {
                multiAccessPath = new ArrayList<>(multiAccessPath);
                EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
                multiAccessPath.add(entityDefinition.getAccessPath());
                AbstractDelegateRepository<?, ?> delegateRepository = (AbstractDelegateRepository<?, ?>) configuredRepository.getRepository();
                EntityPropertyLocation entityPropertyLocation = doFindEntityPropertyLocation(multiAccessPath, configuredRepository, delegateRepository, fieldName);
                if (entityPropertyLocation != null) {
                    return entityPropertyLocation;
                }
            }
        } else {
            String prefixAccessPath = StrUtil.join("", multiAccessPath);
            String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";
            ConfiguredRepository belongConfiguredRepository = findBelongConfiguredRepository(entityPropertyChain);
            EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
            boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;
            return new EntityPropertyLocation(multiAccessPath, prefixAccessPath, forwardParent, parentAccessPath,
                    parentConfiguredRepository, abstractDelegateRepository, entityPropertyChain, belongConfiguredRepository);
        }
        return null;
    }

    protected ConfiguredRepository findBelongConfiguredRepository(EntityPropertyChain entityPropertyChain) {
        String belongAccessPath = getBelongAccessPath(entityPropertyChain.getAccessPath());
        return configuredRepositoryMap.get(belongAccessPath);
    }

}
