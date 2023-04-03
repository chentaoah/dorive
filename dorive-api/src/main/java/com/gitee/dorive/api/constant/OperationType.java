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

package com.gitee.dorive.api.constant;

public interface OperationType {
    int NONE = 0x00000000;
    int SELECT = 0x00000001;
    int INSERT = 0x00000002;
    int UPDATE = 0x00000004;
    int INSERT_OR_UPDATE = INSERT | UPDATE;
    int DELETE = 0x00000008;
    int UPDATE_OR_DELETE = UPDATE | DELETE;
    int INSERT_OR_UPDATE_OR_DELETE = INSERT | UPDATE | DELETE;
    int FORCE_IGNORE = 0x00000010;
    int FORCE_INSERT = 0x00000010 | INSERT;
    int IGNORE_ROOT = 0x00000020;
    int INCLUDE_ROOT = 0x00000040;
}