package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;

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

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return this;
    }

}
