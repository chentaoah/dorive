package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        allEntityPropertyChainMap.values().forEach(entityPropertyChain ->
                fieldEntityPropertyChainMap.putIfAbsent(entityPropertyChain.getFieldName(), entityPropertyChain));
    }

}
