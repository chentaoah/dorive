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
package com.gitee.dorive.core.entity.element;

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Field;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "lastPropertyChain")
public class PropertyChain extends Property implements PropProxy {

    protected PropertyChain lastPropertyChain;
    protected Class<?> entityClass;
    protected String accessPath;
    protected EntityDefinition entityDefinition;
    protected PropProxy propProxy;

    public PropertyChain(PropertyChain lastPropertyChain, Class<?> entityClass, String accessPath, Field declaredField) {
        super(declaredField);
        this.lastPropertyChain = lastPropertyChain;
        this.entityClass = entityClass;
        this.accessPath = accessPath;
        this.entityDefinition = EntityDefinition.newEntityDefinition(declaredField);
        if (entityDefinition != null) {
            newPropertyProxy();
        }
    }

    public void newPropertyProxy() {
        if (propProxy == null) {
            propProxy = PropProxyFactory.newPropProxy(entityClass, declaredField);
            if (lastPropertyChain != null) {
                lastPropertyChain.newPropertyProxy();
            }
        }
    }

    public boolean isAnnotatedEntity() {
        return entityDefinition != null;
    }

    @Override
    public Object getValue(Object entity) {
        if (lastPropertyChain != null) {
            entity = lastPropertyChain.getValue(entity);
        }
        return entity != null ? propProxy.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object value) {
        if (lastPropertyChain != null) {
            entity = lastPropertyChain.getValue(entity);
        }
        if (entity != null) {
            propProxy.setValue(entity, value);
        }
    }

}
