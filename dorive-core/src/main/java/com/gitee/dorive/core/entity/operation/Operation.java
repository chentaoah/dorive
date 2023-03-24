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

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Operation {

    public static final int NONE = 0x00000000;
    public static final int SELECT = 0x00000001;
    public static final int INSERT = 0x00000002;
    public static final int UPDATE = 0x00000004;
    public static final int DELETE = 0x00000008;
    public static final int INSERT_OR_UPDATE = INSERT | UPDATE;
    public static final int UPDATE_OR_DELETE = UPDATE | DELETE;
    public static final int INSERT_OR_UPDATE_OR_DELETE = INSERT | UPDATE | DELETE;
    public static final int FORCE_IGNORE = 0x00000010;
    public static final int FORCE_INSERT = 0x00000010 | INSERT;
    public static final int IGNORE_ROOT = 0x00000020;
    public static final int INCLUDE_ROOT = 0x00000040;

    private int type;
    private Object entity;

}
