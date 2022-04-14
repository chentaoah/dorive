package com.gitee.spring.domain.event.entity;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RepositoryEvent extends ApplicationEvent {

    private String methodName;
    private OperationType operationType;
    private BoundedContext boundedContext;
    private Object entity;
    private Object example;
    private Object primaryKey;

    public RepositoryEvent(AbstractRepository<Object, Object> repository) {
        super(repository);
    }

}
