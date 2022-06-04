package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;

import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected List<ConfiguredRepository> delegateConfiguredRepositories = new ArrayList<>();

    @Override
    protected EntityPropertyChain newEntityPropertyChain(Class<?> lastEntityClass, Field declaredField,
                                                         String accessPath, Class<?> entityClass, String fieldName) {
        EntityPropertyChain entityPropertyChain = super.newEntityPropertyChain(lastEntityClass, declaredField, accessPath, entityClass, fieldName);
        fieldEntityPropertyChainMap.putIfAbsent(fieldName, entityPropertyChain);
        return entityPropertyChain;
    }

    @Override
    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        if (configuredRepository.getRepository() instanceof AbstractDelegateRepository) {
            delegateConfiguredRepositories.add(configuredRepository);
        }
        return super.processConfiguredRepository(configuredRepository);
    }

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return this;
    }

}
