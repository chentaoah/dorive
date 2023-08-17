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

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EntityResolver {

    private Map<String, EntityEle> entityEleMap = new LinkedHashMap<>();

    public EntityResolver(EntityType entityType) {
        this.entityEleMap.put("/", entityType);
        resolve("", entityType);
    }

    private void resolve(String lastAccessPath, EntityType entityType) {
        for (EntityField entityField : entityType.getEntityFieldMap().values()) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            entityEleMap.put(accessPath, entityField);
            EntityType fieldEntityType = entityField.getEntityType();
            if (fieldEntityType != null) {
                resolve(accessPath, fieldEntityType);
            }
        }
    }

}
