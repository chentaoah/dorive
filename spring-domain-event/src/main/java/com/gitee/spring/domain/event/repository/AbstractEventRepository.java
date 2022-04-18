package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.repository.AbstractChainRepository;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractEventRepository<E, PK> extends AbstractChainRepository<E, PK> {

    protected boolean enableEvent;

    @Override
    public void afterPropertiesSet() throws Exception {
        EnableEvent enableEvent = AnnotationUtils.getAnnotation(this.getClass(), EnableEvent.class);
        this.enableEvent = enableEvent != null;
        super.afterPropertiesSet();
    }

    @Override
    protected ConfiguredRepository newConfiguredRepository(EntityPropertyChain entityPropertyChain,
                                                           EntityDefinition entityDefinition,
                                                           EntityMapper entityMapper,
                                                           EntityAssembler entityAssembler,
                                                           AbstractRepository<Object, Object> repository) {
        if (enableEvent) {
            repository = new EventRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository, applicationContext);
        }
        return super.newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
    }

}
