package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected List<ConfiguredRepository> delegateConfiguredRepositories = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        allEntityPropertyChainMap.values().forEach(entityPropertyChain ->
                fieldEntityPropertyChainMap.putIfAbsent(entityPropertyChain.getFieldName(), entityPropertyChain));
    }

    @Override
    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        if (configuredRepository.getRepository() instanceof AbstractDelegateRepository) {
            delegateConfiguredRepositories.add(configuredRepository);
        }
        return super.processConfiguredRepository(configuredRepository);
    }

}
