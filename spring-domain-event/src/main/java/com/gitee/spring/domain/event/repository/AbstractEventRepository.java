package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.repository.AbstractChainRepository;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.ConfigurableRepository;
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
    protected ConfigurableRepository newConfigurableRepository(EntityPropertyChain entityPropertyChain,
                                                               EntityDefinition entityDefinition,
                                                               EntityMapper entityMapper,
                                                               EntityAssembler entityAssembler,
                                                               AbstractRepository<Object, Object> repository) {
        repository = enableEvent ? new DefaultEventRepository(repository, applicationContext) : repository;
        return new ConfigurableRepository(repository, entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
    }

}
