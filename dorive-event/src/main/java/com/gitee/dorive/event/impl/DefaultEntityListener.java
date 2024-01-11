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

package com.gitee.dorive.event.impl;

import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.event.api.EntityListener;
import com.gitee.dorive.event.entity.ExecutorEvent;

public abstract class DefaultEntityListener<E> implements EntityListener {

    public enum OperationType {UNKNOWN, INSERT, UPDATE, DELETE,}

    @Override
    public Class<?> subscribe() {
        return ReflectUtils.getFirstArgumentType(this.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(ExecutorEvent executorEvent) {
        Context context = executorEvent.getContext();
        Operation operation = executorEvent.getOperation();
        OperationType operationType = OperationType.UNKNOWN;
        if (operation instanceof Insert) {
            operationType = OperationType.INSERT;

        } else if (operation instanceof Update) {
            operationType = OperationType.UPDATE;

        } else if (operation instanceof Delete) {
            operationType = OperationType.DELETE;
        }
        onExecution(context, operationType, (E) operation.getEntity());
    }

    protected abstract void onExecution(Context context, OperationType operationType, E entity);

}
