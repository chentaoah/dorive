/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.impl.publisher;

import com.gitee.dorive.base.v1.event.api.EventPublisher;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.Delete;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.base.v1.executor.entity.eop.Insert;
import com.gitee.dorive.base.v1.executor.entity.eop.Update;
import com.gitee.dorive.event.v1.entity.BaseEvent;
import com.gitee.dorive.event.v1.entity.executor.ExecutorDeleteEvent;
import com.gitee.dorive.event.v1.entity.executor.ExecutorInsertEvent;
import com.gitee.dorive.event.v1.entity.executor.ExecutorUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ExecutorEventPublisher implements EventPublisher {

    private final List<ApplicationEventPublisher> publishers;

    @Override
    public void publishEvent(Class<?> entityClass, Context context, EntityOp entityOp) {
        Object event = newEvent(entityClass, context, entityOp);
        if (event != null) {
            for (ApplicationEventPublisher publisher : publishers) {
                publisher.publishEvent(event);
            }
        }
    }

    private Object newEvent(Class<?> entityClass, Context context, EntityOp entityOp) {
        BaseEvent<?> baseEvent = null;
        if (entityOp instanceof Insert) {
            baseEvent = new ExecutorInsertEvent<>();

        } else if (entityOp instanceof Update) {
            baseEvent = new ExecutorUpdateEvent<>();

        } else if (entityOp instanceof Delete) {
            baseEvent = new ExecutorDeleteEvent<>();
        }
        if (baseEvent != null) {
            baseEvent.setRoot(entityOp.isRoot());
            baseEvent.setEntityClass(entityClass);
            baseEvent.setContext(context);
            baseEvent.setEntityOp(entityOp);
        }
        return baseEvent;
    }

}
