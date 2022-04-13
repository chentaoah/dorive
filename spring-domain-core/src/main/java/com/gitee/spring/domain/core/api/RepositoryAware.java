package com.gitee.spring.domain.core.api;

public interface RepositoryAware {

    void setRepository(IRepository<?, ?> repository);

}
