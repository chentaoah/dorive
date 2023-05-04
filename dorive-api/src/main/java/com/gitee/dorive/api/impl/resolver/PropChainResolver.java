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

package com.gitee.dorive.api.impl.resolver;

import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.api.entity.element.PropChain;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropChainResolver {

    private Map<String, PropChain> propChainMap = new LinkedHashMap<>();

    public PropChainResolver(EntityType entityType) {
        resolve("", entityType);
    }

    private void resolve(String lastAccessPath, EntityType entityType) {
        PropChain lastPropChain = propChainMap.get(lastAccessPath);
        for (EntityField entityField : entityType.getEntityFields().values()) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            PropChain propChain = new PropChain(lastPropChain, entityType, accessPath, entityField);
            propChainMap.put(accessPath, propChain);
            if (EntityField.filter(entityField.getType()) && !entityField.isAnnotatedEntity()) {
                resolve(accessPath, entityField.getEntityType());
            }
        }
    }

}
