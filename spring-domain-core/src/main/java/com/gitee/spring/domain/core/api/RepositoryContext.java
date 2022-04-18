package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;

public interface RepositoryContext {

    ConfiguredRepository getRepository(Class<?> entityClass);

}
