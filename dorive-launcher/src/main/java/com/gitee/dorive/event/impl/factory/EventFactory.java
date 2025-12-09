/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.event.impl.factory;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.event.entity.BaseEvent;
import com.gitee.dorive.event.entity.ext.*;

public class EventFactory {

    public static BaseEvent<?> newExecutorEvent(Object source, boolean root, Class<?> entityClass, Context context, EntityOp entityOp) {
        BaseEvent<?> baseEvent = null;
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

    public static BaseEvent<?> newRepositoryEvent(Object source, boolean root, Class<?> entityClass, Context context, EntityOp entityOp) {
        BaseEvent<?> baseEvent = null;
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
