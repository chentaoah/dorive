package com.gitee.spring.domain.proxy.repository;

import com.gitee.spring.domain.proxy.annotation.EnableEvent;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityMapper;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
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
    protected DefaultRepository doNewDefaultRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                       EntityMapper entityMapper, EntityAssembler entityAssembler) {
        if (enableEvent) {
            return new DefaultEventRepository(applicationContext, entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
        } else {
            return super.doNewDefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
        }
    }
}
