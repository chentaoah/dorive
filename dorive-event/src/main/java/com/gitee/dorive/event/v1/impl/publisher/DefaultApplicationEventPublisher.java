package com.gitee.dorive.event.v1.impl.publisher;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.event.v1.entity.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DefaultApplicationEventPublisher implements ApplicationEventPublisher {

    private final Class<?> source;
    private final Class<?> target;
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(@NonNull Object event) {
        if (source.isAssignableFrom(event.getClass())) {
            BaseEvent<?> baseEvent = (BaseEvent<?>) event;
            EntityOp entityOp = baseEvent.getEntityOp();
            List<?> entities = entityOp.getEntities();
            if (entities.size() == 1) {
                Object newEvent = BeanUtil.copyProperties(entities.get(0), target);
                if (newEvent != null) {
                    publisher.publishEvent(newEvent);
                }
            }
        }
    }

}
