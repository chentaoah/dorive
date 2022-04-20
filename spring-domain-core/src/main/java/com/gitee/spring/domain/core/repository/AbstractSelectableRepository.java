package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSelectableRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected List<AbstractSelectableRepository<?, ?>> abstractSelectableRepositories = new ArrayList<>();
    protected Map<Class<?>, ConfiguredRepository> classRepositoryMap = new LinkedHashMap<>();

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
        if (repository instanceof AbstractSelectableRepository) {
            abstractSelectableRepositories.add((AbstractSelectableRepository<?, ?>) repository);
        }
        ConfiguredRepository configuredRepository = super.newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
        classRepositoryMap.put(entityDefinition.getGenericEntityClass(), configuredRepository);
        return configuredRepository;
    }

}
