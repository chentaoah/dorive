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

package com.gitee.dorive.def.entity;

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.EntityField;
import com.gitee.dorive.api.entity.EntityType;
import com.gitee.dorive.api.factory.PropProxyFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(exclude = "lastPropChain")
@EqualsAndHashCode(callSuper = false)
public class PropertyChain implements PropProxy {

    private PropertyChain lastPropChain;
    private EntityType entityType;
    private String accessPath;
    private EntityField entityField;
    private PropProxy propProxy;

    public PropertyChain(PropertyChain lastPropChain, EntityType entityType, String accessPath, EntityField entityField) {
        this.lastPropChain = lastPropChain;
        this.entityType = entityType;
        this.accessPath = accessPath;
        this.entityField = entityField;
        if (entityField.isEntityDef()) {
            newPropProxy();
        }
    }

    public void newPropProxy() {
        if (propProxy == null) {
            propProxy = PropProxyFactory.newPropProxy(entityType.getType(), entityField.getField());
            if (lastPropChain != null) {
                lastPropChain.newPropProxy();
            }
        }
    }

    public boolean isEntityDef() {
        return entityField.isEntityDef();
    }

    public boolean isSameType(PropertyChain propChain) {
        return entityField.isSameType(propChain.getEntityField());
    }

    @Override
    public Object getValue(Object entity) {
        if (lastPropChain != null) {
            entity = lastPropChain.getValue(entity);
        }
        return entity != null ? propProxy.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object value) {
        if (lastPropChain != null) {
            entity = lastPropChain.getValue(entity);
        }
        if (entity != null) {
            propProxy.setValue(entity, value);
        }
    }

}
