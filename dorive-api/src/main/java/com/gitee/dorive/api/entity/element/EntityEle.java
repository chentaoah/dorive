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

package com.gitee.dorive.api.entity.element;

import com.gitee.dorive.api.annotation.Aggregate;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class EntityEle {

    private AnnotatedElement element;
    private EntityDef entityDef;
    private boolean aggregated;
    private List<BindingDef> bindingDefs;
    private PropProxy pkProxy;
    private Map<String, String> aliasMap;

    public EntityEle(AnnotatedElement element) {
        this.element = element;
        this.entityDef = EntityDef.fromElement(element);
        this.aggregated = (entityDef != null && entityDef.isAggregated()) || element.isAnnotationPresent(Aggregate.class);
        this.bindingDefs = BindingDef.fromElement(element);
    }

    public boolean isAnnotatedEntity() {
        return entityDef != null;
    }

    public boolean isAggregated() {
        return aggregated;
    }

    public void initialize() {
        if (entityDef != null && pkProxy == null) {
            doInitialize();
        }
    }

    public String toAlias(String property) {
        return aliasMap.getOrDefault(property, property);
    }

    public List<String> toAliases(List<String> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<String> aliases = new ArrayList<>(properties.size());
            for (String property : properties) {
                String alias = toAlias(property);
                aliases.add(alias);
            }
            return aliases;
        }
        return properties;
    }

    public Map<String, String> newAliasFieldMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        aliasMap.forEach((key, value) -> map.put(value, key));
        return map;
    }

    protected abstract void doInitialize();

    public abstract boolean isCollection();

    public abstract Class<?> getGenericType();

    public abstract EntityType getEntityType();

}
