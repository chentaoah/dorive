package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
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

    @Override
    public AbstractRepository<Object, Object> getProxyRepository() {
        AbstractRepository<Object, Object> abstractRepository = super.getProxyRepository();
        if (abstractRepository instanceof ConfiguredRepository) {
            return ((ConfiguredRepository) abstractRepository).getProxyRepository();
        }
        return abstractRepository;
    }

    private Object processExample(BoundedContext boundedContext, Object example) {
        if (example instanceof EntityExample) {
            EntityExample entityExample = (EntityExample) example;
            if (entityExample.isEmptyQuery()) {
                return null;
            } else if (!entityDefinition.isUseEntityExample()) {
                return entityMapper.buildExample(boundedContext, entityDefinition, entityExample);
            }
        }
        return example;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        example = processExample(boundedContext, example);
        return example != null ? super.selectByExample(boundedContext, example) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        example = processExample(boundedContext, example);
        if (example == null) {
            return (T) entityMapper.newPageOfEntities(page, Collections.emptyList());
        } else {
            return super.selectPageByExample(boundedContext, example, page);
        }
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        example = processExample(new BoundedContext(), example);
        return example != null ? super.updateByExample(entity, example) : 0;
    }

    @Override
    public int deleteByExample(Object example) {
        example = processExample(new BoundedContext(), example);
        return example != null ? super.deleteByExample(example) : 0;
    }

}
