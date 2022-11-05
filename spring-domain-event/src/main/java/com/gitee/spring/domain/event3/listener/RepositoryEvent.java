package com.gitee.spring.domain.event3.listener;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.operation.Condition;
import com.gitee.spring.domain.event3.repository.EventRepository;
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
