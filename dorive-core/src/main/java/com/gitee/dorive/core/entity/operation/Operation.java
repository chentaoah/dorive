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

import lombok.Data;

@Data
public class Operation {

    public interface Type {
        int NONE = 0x00000000;
        int SELECT = 0x00000001;
        int INSERT = 0x00000002;
        int UPDATE = 0x00000004;
        int INSERT_OR_UPDATE = INSERT | UPDATE;
        int DELETE = 0x00000008;
        int UPDATE_OR_DELETE = UPDATE | DELETE;
        int INSERT_OR_UPDATE_OR_DELETE = INSERT | UPDATE | DELETE;
        int FORCE_INSERT = 0x00000010 | INSERT;
    }

    public enum RootControl {NONE, INCLUDE_ROOT, IGNORE_ROOT,}

    private int type;
    private Object entity;
    private RootControl rootControl = RootControl.NONE;

    public Operation(int type, Object entity) {
        this.type = type;
        this.entity = entity;
    }

    public boolean isInsertContext() {
        return (type & Type.INSERT) != 0;
    }

    public boolean isForceInsert() {
        return type == Type.FORCE_INSERT;
    }

    public void includeRoot() {
        rootControl = RootControl.INCLUDE_ROOT;
    }

    public boolean isIncludeRoot() {
        return rootControl == RootControl.INCLUDE_ROOT;
    }

    public void ignoreRoot() {
        rootControl = RootControl.IGNORE_ROOT;
    }

    public boolean isIgnoreRoot() {
        return rootControl == RootControl.IGNORE_ROOT;
    }

}
