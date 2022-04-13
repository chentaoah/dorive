package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.repository.DefaultRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RepositoryEvent extends ApplicationEvent {

    private EntityEvent entityEvent;

    public RepositoryEvent(DefaultRepository defaultRepository) {
        super(defaultRepository);
    }

}
