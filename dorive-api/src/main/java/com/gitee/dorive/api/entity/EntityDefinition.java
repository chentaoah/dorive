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

package com.gitee.dorive.api.entity;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class EntityDefinition {
    private String name;
    private String sourceName;
    private String factoryName;
    private String repositoryName;
    private int priority;
    private String genericTypeName;
    private String primaryKey;
    private List<FieldDefinition> fieldDefinitions;
    private List<FieldEntityDefinition> fieldEntityDefinitions;

    public boolean isAutoDiscovery() {
        return false;
    }

    public List<BindingDefinition> getBindingDefinitions() {
        return Collections.emptyList();
    }

    public String getSortBy() {
        return null;
    }

    public String getOrder() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public String getFieldName() {
        return null;
    }
}
