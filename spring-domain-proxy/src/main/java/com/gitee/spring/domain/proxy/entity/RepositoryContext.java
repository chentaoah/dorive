package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.IRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryContext {
    private IRepository<?, ?> repository;
    private BoundedContext boundedContext;
}
