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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public abstract class EntityEle {

    private AnnotatedElement element;
    private EntityDef entityDef;
    private boolean aggregated;
    private List<BindingDef> bindingDefs;
    private PropProxy pkProxy;
    private Map<String, String> fieldAliasMapping;

    public EntityEle(AnnotatedElement element) {
        this.element = element;
        this.entityDef = EntityDef.fromElement(element);
        this.aggregated = (entityDef != null && entityDef.isRepositoryDef()) || isAggregateDef();
        this.bindingDefs = BindingDef.fromElement(element);
    }

    public boolean isEntityDef() {
        return entityDef != null;
    }

    public boolean isAggregateDef() {
        return element.isAnnotationPresent(Aggregate.class);
    }

    public void initialize() {
        if (entityDef != null && pkProxy == null) {
            doInitialize();
        }
    }

    public boolean hasField(String field) {
        return fieldAliasMapping.containsKey(field);
    }

    public String toAlias(String field) {
        return fieldAliasMapping.getOrDefault(field, field);
    }

    public List<String> toAliases(List<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            List<String> aliases = new ArrayList<>(fields.size());
            for (String field : fields) {
                String alias = toAlias(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    public Set<String> toAliases(Set<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Set<String> aliases = new LinkedHashSet<>(fields.size() * 4 / 3 + 1);
            for (String field : fields) {
                String alias = toAlias(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    protected abstract void doInitialize();

    public abstract boolean isCollection();

    public abstract Class<?> getGenericType();

    public abstract EntityType getEntityType();

    public abstract Map<String, EntityField> getEntityFieldMap();

}
