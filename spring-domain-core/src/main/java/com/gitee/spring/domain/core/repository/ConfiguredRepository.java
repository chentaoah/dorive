package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConfiguredRepository extends ProxyRepository {

    protected AbstractContextRepository<?, ?> abstractContextRepository;
    protected EntityPropertyChain entityPropertyChain;
    protected EntityDefinition entityDefinition;
    protected EntityMapper entityMapper;
    protected EntityAssembler entityAssembler;

    public ConfiguredRepository(AbstractContextRepository<?, ?> abstractContextRepository,
                                EntityPropertyChain entityPropertyChain,
                                EntityDefinition entityDefinition,
                                EntityMapper entityMapper,
                                EntityAssembler entityAssembler,
                                AbstractRepository<Object, Object> repository) {
        super(repository);
        this.abstractContextRepository = abstractContextRepository;
        this.entityPropertyChain = entityPropertyChain;
        this.entityDefinition = entityDefinition;
        this.entityMapper = entityMapper;
        this.entityAssembler = entityAssembler;
    }

    public ConfiguredRepository(ConfiguredRepository configuredRepository) {
        super(configuredRepository);
        this.abstractContextRepository = configuredRepository.getAbstractContextRepository();
        this.entityPropertyChain = configuredRepository.getEntityPropertyChain();
        this.entityDefinition = configuredRepository.getEntityDefinition();
        this.entityMapper = configuredRepository.getEntityMapper();
        this.entityAssembler = configuredRepository.getEntityAssembler();
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        if (!entityDefinition.isUseEntityExample() && example instanceof EntityExample) {
            example = ((EntityExample) example).buildExample();
        }
        return super.selectByExample(boundedContext, example);
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        if (!entityDefinition.isUseEntityExample() && example instanceof EntityExample) {
            example = ((EntityExample) example).buildExample();
        }
        return super.selectPageByExample(boundedContext, example, page);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        if (!entityDefinition.isUseEntityExample() && example instanceof EntityExample) {
            example = ((EntityExample) example).buildExample();
        }
        return super.updateByExample(entity, example);
    }

    @Override
    public int deleteByExample(Object example) {
        if (!entityDefinition.isUseEntityExample() && example instanceof EntityExample) {
            example = ((EntityExample) example).buildExample();
        }
        return super.deleteByExample(example);
    }

}
