package com.gitee.spring.domain.core.config;

import com.gitee.spring.domain.core.repository.AbstractGenericRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryContext implements ApplicationContextAware, InitializingBean {

    private static final Map<Class<?>, AbstractGenericRepository<?, ?>> CLASS_REPOSITORY_MAP = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("all")
    public void afterPropertiesSet() throws Exception {
        Map<String, AbstractGenericRepository> beansOfType = applicationContext.getBeansOfType(AbstractGenericRepository.class);
        for (AbstractGenericRepository<?, ?> abstractGenericRepository : beansOfType.values()) {
            CLASS_REPOSITORY_MAP.put(abstractGenericRepository.getEntityClass(), abstractGenericRepository);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractGenericRepository<?, ?>> T getRepository(Class<?> entityClass) {
        return (T) CLASS_REPOSITORY_MAP.get(entityClass);
    }

}
