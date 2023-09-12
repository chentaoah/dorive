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

package com.gitee.dorive.core.entity.operation;

import com.gitee.dorive.api.constant.OperationType;
import lombok.Data;

@Data
public class Operation {

    private int type;
    private int realType;
    private Object entity;

    public Operation(int type, Object entity) {
        this.type = type;
        this.realType = type;
        this.entity = entity;
    }

    public boolean isIncludeRoot() {
        return (type & OperationType.INCLUDE_ROOT) != 0;
    }

    public boolean isIgnoreRoot() {
        return (type & OperationType.IGNORE_ROOT) != 0;
    }

    public boolean isInsertContext() {
        return (realType & OperationType.INSERT) != 0;
    }

    public int includeRoot() {
        return realType | OperationType.INCLUDE_ROOT;
    }

    public int ignoreRoot() {
        return realType | OperationType.IGNORE_ROOT;
    }

    public boolean isForceInsert() {
        return realType == OperationType.FORCE_INSERT;
    }

}
