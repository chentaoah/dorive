package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguredRepository extends ProxyRepository {

    protected EntityPropertyChain entityPropertyChain;
    protected EntityDefinition entityDefinition;
    protected EntityMapper entityMapper;
    protected EntityAssembler entityAssembler;

    public ConfiguredRepository(EntityPropertyChain entityPropertyChain,
                                EntityDefinition entityDefinition,
                                EntityMapper entityMapper,
                                EntityAssembler entityAssembler,
                                AbstractRepository<Object, Object> repository) {
        super(repository);
        this.entityPropertyChain = entityPropertyChain;
        this.entityDefinition = entityDefinition;
        this.entityMapper = entityMapper;
        this.entityAssembler = entityAssembler;
    }

}
