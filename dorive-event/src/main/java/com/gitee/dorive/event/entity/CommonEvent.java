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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.api.constant.enums.OperationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class CommonEvent extends ApplicationEvent {

    private Context context;
    private Operation operation;
    private OperationType operationType;

    public CommonEvent(Object source) {
        super(source);
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
        if (operation instanceof EntityOp) {
            this.operationType = OperationType.UNKNOWN;
            if (operation instanceof Insert) {
                this.operationType = OperationType.INSERT;

            } else if (operation instanceof Update) {
                this.operationType = OperationType.UPDATE;

            } else if (operation instanceof Delete) {
                this.operationType = OperationType.DELETE;
            }
        }
    }

    public List<?> getEntities() {
        EntityOp entityOp = (EntityOp) operation;
        return entityOp.getEntities();
    }

}
