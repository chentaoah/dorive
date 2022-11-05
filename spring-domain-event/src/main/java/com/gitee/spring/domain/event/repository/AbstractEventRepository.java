package com.gitee.spring.domain.event.repository;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.repository.AbstractGenericRepository;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.DefaultRepository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractEventRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    protected boolean enableEvent;

    @Override
    public void afterPropertiesSet() throws Exception {
        EnableEvent enableEvent = AnnotationUtils.getAnnotation(this.getClass(), EnableEvent.class);
        this.enableEvent = enableEvent != null;
        super.afterPropertiesSet();
    }

    @Override
    protected AbstractRepository<Object, Object> postProcessRepository(AbstractRepository<Object, Object> repository) {
        if (enableEvent && (repository instanceof DefaultRepository)) {
            EventRepository eventRepository = new EventRepository(applicationContext);
            BeanUtil.copyProperties(repository, eventRepository);
            eventRepository.setProxyRepository(repository);
            return eventRepository;
        }
        return repository;
    }

}
