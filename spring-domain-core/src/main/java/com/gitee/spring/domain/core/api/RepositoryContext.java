package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.repository.DefaultRepository;

public interface RepositoryContext {

    DefaultRepository getRepository(Class<?> entityClass);

}
