package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.repository.DefaultRepository;

public interface RepositoryRoute {

    DefaultRepository getRepository(Class<?> entityClass);

}
