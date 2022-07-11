package com.gitee.spring.domain.core.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.ReflectionUtils;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<Class<?>, AbstractDelegateRepository<?, ?>> delegateRepositoryMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        resolveDelegateRepositoryMap();
    }

    protected void resolveDelegateRepositoryMap() {
        delegateRepositoryMap.put(entityClass, this);
        Class<?> repositoryClass = this.getClass();
        ReflectionUtils.doWithLocalFields(repositoryClass, declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractDelegateRepository.class.isAssignableFrom(fieldClass)) {
                Object bean = applicationContext.getBean(fieldClass);
                AbstractDelegateRepository<?, ?> repository = (AbstractDelegateRepository<?, ?>) bean;
                Class<?> fieldEntityClass = repository.getEntityClass();
                if (entityClass.isAssignableFrom(fieldEntityClass)) {
                    delegateRepositoryMap.put(fieldEntityClass, repository);
                }
            }
        });
    }

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Class<?> entityClass) {
        return delegateRepositoryMap.get(entityClass);
    }

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return adaptiveRepository(rootEntity.getClass());
    }

}
