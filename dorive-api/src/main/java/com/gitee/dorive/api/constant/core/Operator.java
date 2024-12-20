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

package com.gitee.dorive.api.constant.core;

public interface Operator {
    String EQ = "=";
    String NE = "<>";
    String GT = ">";
    String GE = ">=";
    String LT = "<";
    String LE = "<=";
    String IN = "IN";
    String NOT_IN = "NOT IN";
    String LIKE = "LIKE";
    String NOT_LIKE = "NOT LIKE";
    String IS_NULL = "IS NULL";
    String IS_NOT_NULL = "IS NOT NULL";
    String MULTI_IN = "MULTI_IN";
    String MULTI_NOT_IN = "MULTI_NOT_IN";
    String AND = "AND";
    String OR = "OR";
}
