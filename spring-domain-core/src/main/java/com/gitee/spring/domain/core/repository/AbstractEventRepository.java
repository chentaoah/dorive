package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.annotation.EnableEvent;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractEventRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected boolean enableEvent;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EnableEvent enableEvent = AnnotationUtils.getAnnotation(this.getClass(), EnableEvent.class);
        this.enableEvent = enableEvent != null;
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
