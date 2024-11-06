package com.gitee.dorive.event.impl.factory;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.event.entity.BaseEvent;
import com.gitee.dorive.event.entity.ext.*;

public class EventFactory {

    public static BaseEvent newExecutorEvent(Object source, boolean root, Class<?> entityClass, Context context, EntityOp entityOp) {
        BaseEvent baseEvent = null;
        if (entityOp instanceof Insert) {
            baseEvent = new ExecutorInsertEvent<>(source);

        } else if (entityOp instanceof Update) {
            baseEvent = new ExecutorUpdateEvent<>(source);

        } else if (entityOp instanceof Delete) {
            baseEvent = new ExecutorDeleteEvent<>(source);
        }
        if (baseEvent != null) {
            baseEvent.setRoot(root);
            baseEvent.setEntityClass(entityClass);
            baseEvent.setContext(context);
            baseEvent.setEntityOp(entityOp);
        }
        return baseEvent;
    }

    public static BaseEvent newRepositoryEvent(Object source, boolean root, Class<?> entityClass, Context context, EntityOp entityOp) {
        BaseEvent baseEvent = null;
        if (entityOp instanceof Insert) {
            baseEvent = new RepositoryInsertEvent<>(source);

        } else if (entityOp instanceof Update) {
            baseEvent = new RepositoryUpdateEvent<>(source);

        } else if (entityOp instanceof Delete) {
            baseEvent = new RepositoryDeleteEvent<>(source);
        }
        if (baseEvent != null) {
            baseEvent.setRoot(root);
            baseEvent.setEntityClass(entityClass);
            baseEvent.setContext(context);
            baseEvent.setEntityOp(entityOp);
        }
        return baseEvent;
    }

}
