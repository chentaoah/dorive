package com.gitee.spring.domain.proxy.api;

public interface RepositoryAware {

    void setRepository(IRepository<?, ?> repository);

}
