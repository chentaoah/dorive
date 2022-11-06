package com.gitee.spring.domain.core.config;

import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryContext implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    private static final Map<Class<?>, AbstractContextRepository<?, ?>> CLASS_REPOSITORY_MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterPropertiesSet() {
        Map<String, AbstractContextRepository> beansOfType = applicationContext.getBeansOfType(AbstractContextRepository.class);
        for (AbstractContextRepository<?, ?> repository : beansOfType.values()) {
            CLASS_REPOSITORY_MAP.put(repository.getEntityClass(), repository);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R extends AbstractContextRepository<E, ?>, E> R getRepository(Class<E> entityClass) {
        return (R) CLASS_REPOSITORY_MAP.get(entityClass);
    }

}
