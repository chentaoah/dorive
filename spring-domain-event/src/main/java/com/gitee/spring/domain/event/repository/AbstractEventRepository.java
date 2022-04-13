package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.repository.AbstractChainRepository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.repository.DefaultRepository;
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
    protected DefaultRepository newDefaultRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                     EntityMapper entityMapper, EntityAssembler entityAssembler) {
        if (enableEvent) {
            return new DefaultEventRepository(applicationContext, entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
        } else {
            return super.newDefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
        }
    }
}
