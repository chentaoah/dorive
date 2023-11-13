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

    public static final int UNKNOWN = 0;
    public static final int INCLUDE_ROOT = 1;
    public static final int IGNORE_ROOT = 2;

    private int type;
    private Object entity;
    private int rootType;

    public Operation(int type, Object entity) {
        this.type = type;
        this.entity = entity;
        this.rootType = UNKNOWN;
    }

    public boolean isInsertContext() {
        return (type & OperationType.INSERT) != 0;
    }

    public boolean isForceInsert() {
        return type == OperationType.FORCE_INSERT;
    }

    public boolean isIncludeRoot() {
        return rootType == 1;
    }

    public boolean isIgnoreRoot() {
        return rootType == 2;
    }

}
