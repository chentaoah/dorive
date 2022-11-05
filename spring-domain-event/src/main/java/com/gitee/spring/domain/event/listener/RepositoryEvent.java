package com.gitee.spring.domain.event.listener;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.operation.Condition;
import com.gitee.spring.domain.event.repository.EventRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RepositoryEvent extends ApplicationEvent {

    private String methodName;
    private BoundedContext boundedContext;
    private Condition condition;

    public RepositoryEvent(EventRepository eventRepository) {
        super(eventRepository);
    }

}
