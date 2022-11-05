package com.gitee.spring.domain.core3.impl.resolver;

import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DelegateResolver {

    private AbstractContextRepository<?, ?> repository;

    private Map<Class<?>, AbstractContextRepository<?, ?>> delegateRepositoryMap = new LinkedHashMap<>();

    public DelegateResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveRepositoryMap() {
        delegateRepositoryMap.put(repository.getEntityClass(), repository);
        ReflectionUtils.doWithLocalFields(repository.getClass(), declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractContextRepository.class.isAssignableFrom(fieldClass)) {
                ApplicationContext applicationContext = repository.getApplicationContext();
                Object beanInstance = applicationContext.getBean(fieldClass);
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) beanInstance;
                Class<?> fieldEntityClass = abstractContextRepository.getEntityClass();
                if (repository.getEntityClass().isAssignableFrom(fieldEntityClass)) {
                    delegateRepositoryMap.put(fieldEntityClass, abstractContextRepository);
                }
            }
        });
    }

    public boolean isDelegated() {
        return delegateRepositoryMap.size() > 1;
    }

    public AbstractContextRepository<?, ?> delegateRepository(Object rootEntity) {
        return delegateRepositoryMap.get(rootEntity.getClass());
    }

}
