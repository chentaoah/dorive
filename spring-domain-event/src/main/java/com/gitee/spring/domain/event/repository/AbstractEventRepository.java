package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.repository.AbstractBatchRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractEventRepository<E, PK> extends AbstractBatchRepository<E, PK> {

    protected boolean enableEvent;

    @Override
    public void afterPropertiesSet() throws Exception {
        EnableEvent enableEvent = AnnotationUtils.getAnnotation(this.getClass(), EnableEvent.class);
        this.enableEvent = enableEvent != null;
        super.afterPropertiesSet();
    }

    @Override
    protected ConfiguredRepository postProcessRepository(ConfiguredRepository configuredRepository) {
        super.postProcessRepository(configuredRepository);
        return enableEvent ? new EventRepository(configuredRepository, applicationContext) : configuredRepository;
    }

}
