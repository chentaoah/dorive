package com.gitee.spring.domain.core.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.ReflectionUtils;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractDelegateRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<Class<?>, AbstractDelegateRepository<?, ?>> classDelegateRepositoryMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        resolveDelegateRepositoryMap();
    }

    protected void resolveDelegateRepositoryMap() {
        classDelegateRepositoryMap.put(entityClass, this);
        ReflectionUtils.doWithLocalFields(this.getClass(), declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractDelegateRepository.class.isAssignableFrom(fieldClass)) {
                Object beanInstance = applicationContext.getBean(fieldClass);
                AbstractDelegateRepository<?, ?> abstractDelegateRepository = (AbstractDelegateRepository<?, ?>) beanInstance;
                Class<?> fieldEntityClass = abstractDelegateRepository.getEntityClass();
                if (entityClass.isAssignableFrom(fieldEntityClass)) {
                    classDelegateRepositoryMap.put(fieldEntityClass, abstractDelegateRepository);
                }
            }
        });
    }

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Class<?> entityClass) {
        return classDelegateRepositoryMap.get(entityClass);
    }

    protected AbstractDelegateRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return adaptiveRepository(rootEntity.getClass());
    }

}
