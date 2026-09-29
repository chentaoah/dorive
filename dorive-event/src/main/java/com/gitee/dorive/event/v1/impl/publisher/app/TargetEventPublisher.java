/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.impl.publisher.app;

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
public class TargetEventPublisher implements ApplicationEventPublisher {

    private final Class<?> target;
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(@NonNull Object event) {
        BaseEvent<?> baseEvent = (BaseEvent<?>) event;
        EntityOp entityOp = baseEvent.getEntityOp();
        List<?> entities = entityOp.getEntities();
        if (entities != null && !entities.isEmpty()) {
            for (Object entity : entities) {
                Object newEvent = BeanUtil.copyProperties(entity, target);
                if (newEvent != null) {
                    publisher.publishEvent(newEvent);
                }
            }
        }
    }

}
