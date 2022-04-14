package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.repository.ConfigurableRepository;

public interface RepositoryContext {

    ConfigurableRepository getRepository(Class<?> entityClass);

}
