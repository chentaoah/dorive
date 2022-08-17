package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.binder.ContextEntityBinder;
import com.gitee.spring.domain.core.binder.PropertyEntityBinder;
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
    protected List<EntityBinder> allEntityBinders;
    protected List<PropertyEntityBinder> boundEntityBinders;
    protected List<ContextEntityBinder> contextEntityBinders;
    protected List<EntityBinder> boundValueEntityBinders;
    protected PropertyEntityBinder boundIdEntityBinder;
    protected EntityMapper entityMapper;
    protected EntityAssembler entityAssembler;

    public ConfiguredRepository(AbstractRepository<Object, Object> repository,
                                EntityPropertyChain entityPropertyChain,
                                EntityDefinition entityDefinition,
                                List<EntityBinder> allEntityBinders,
                                List<PropertyEntityBinder> boundEntityBinders,
                                List<ContextEntityBinder> contextEntityBinders,
                                List<EntityBinder> boundValueEntityBinders,
                                PropertyEntityBinder boundIdEntityBinder,
                                EntityMapper entityMapper,
                                EntityAssembler entityAssembler) {
        super(repository);
        this.entityPropertyChain = entityPropertyChain;
        this.entityDefinition = entityDefinition;
        this.allEntityBinders = allEntityBinders;
        this.boundEntityBinders = boundEntityBinders;
        this.contextEntityBinders = contextEntityBinders;
        this.boundValueEntityBinders = boundValueEntityBinders;
        this.boundIdEntityBinder = boundIdEntityBinder;
        this.entityMapper = entityMapper;
        this.entityAssembler = entityAssembler;
    }

    public ConfiguredRepository(ConfiguredRepository configuredRepository) {
        super(configuredRepository);
        this.entityPropertyChain = configuredRepository.getEntityPropertyChain();
        this.entityDefinition = configuredRepository.getEntityDefinition();
        this.allEntityBinders = configuredRepository.getAllEntityBinders();
        this.boundEntityBinders = configuredRepository.getBoundEntityBinders();
        this.contextEntityBinders = configuredRepository.getContextEntityBinders();
        this.boundValueEntityBinders = configuredRepository.getBoundValueEntityBinders();
        this.boundIdEntityBinder = configuredRepository.getBoundIdEntityBinder();
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

    private Object buildExample(BoundedContext boundedContext, Object example) {
        if (example instanceof EntityExample) {
            EntityExample entityExample = (EntityExample) example;
            if (entityExample.isEmptyQuery()) {
                return null;
            } else if (!entityDefinition.isUseEntityExample()) {
                return entityMapper.buildExample(boundedContext, entityExample);
            }
        }
        return example;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        example = buildExample(boundedContext, example);
        return example != null ? super.selectByExample(boundedContext, example) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        example = buildExample(boundedContext, example);
        if (example == null) {
            return (T) entityMapper.newPageOfEntities(page, Collections.emptyList());
        } else {
            return super.selectPageByExample(boundedContext, example, page);
        }
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Object example) {
        example = buildExample(new BoundedContext(), example);
        return example != null ? super.updateByExample(boundedContext, entity, example) : 0;
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Object example) {
        example = buildExample(new BoundedContext(), example);
        return example != null ? super.deleteByExample(boundedContext, example) : 0;
    }

}
