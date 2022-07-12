package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    public ConfiguredRepository(ConfiguredRepository configuredRepository) {
        super(configuredRepository);
        this.entityPropertyChain = configuredRepository.getEntityPropertyChain();
        this.entityDefinition = configuredRepository.getEntityDefinition();
        this.entityMapper = configuredRepository.getEntityMapper();
        this.entityAssembler = configuredRepository.getEntityAssembler();
    }

    private Object processExample(Object example) {
        if (!entityDefinition.isUseEntityExample() && example instanceof EntityExample) {
            return ((EntityExample) example).buildExample();
        }
        return example;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        return super.selectByExample(boundedContext, processExample(example));
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        return super.selectPageByExample(boundedContext, processExample(example), page);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        return super.updateByExample(entity, processExample(example));
    }

    @Override
    public int deleteByExample(Object example) {
        return super.deleteByExample(processExample(example));
    }

}
