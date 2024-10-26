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

package com.gitee.dorive.event.entity;

import com.gitee.dorive.api.constant.event.Event;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class EntityEvent extends ApplicationEvent {
    private String publisher;
    private Class<?> entityClass;
    private String name;
    private Context context;
    private boolean root;
    private List<?> entities;

    public static String getEventName(EntityOp entityOp) {
        if (entityOp instanceof Insert) {
            return Event.INSERT;

        } else if (entityOp instanceof Update) {
            return Event.UPDATE;

        } else if (entityOp instanceof Delete) {
            return Event.DELETE;
        }
        return null;
    }

    public EntityEvent(Object source) {
        super(source);
    }
}
