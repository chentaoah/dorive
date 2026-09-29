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
import com.gitee.dorive.event.v1.entity.repository.RepositoryDeleteEvent;
import com.gitee.dorive.event.v1.entity.repository.RepositoryInsertEvent;
import com.gitee.dorive.event.v1.entity.repository.RepositoryUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RepositoryEventPublisher implements EventPublisher {

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
            baseEvent = new RepositoryInsertEvent<>();

        } else if (entityOp instanceof Update) {
            baseEvent = new RepositoryUpdateEvent<>();

        } else if (entityOp instanceof Delete) {
            baseEvent = new RepositoryDeleteEvent<>();
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
