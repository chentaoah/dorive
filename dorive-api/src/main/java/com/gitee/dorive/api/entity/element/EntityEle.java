package com.gitee.dorive.api.entity.element;

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class EntityEle {

    private AnnotatedElement element;
    private EntityDef entityDef;
    private List<BindingDef> bindingDefs;
    private PropProxy pkProxy;
    private Map<String, String> aliasMap;

    public static EntityEle fromElement(AnnotatedElement element) {
        if (element instanceof Class) {
            return EntityType.getInstance((Class<?>) element);

        } else if (element instanceof Field) {
            return new EntityField((Field) element);
        }
        throw new RuntimeException("Unsupported type!");
    }

    public EntityEle(AnnotatedElement element) {
        this.element = element;
        this.entityDef = EntityDef.fromElement(element);
        this.bindingDefs = BindingDef.fromElement(element);
    }

    public boolean isAnnotatedEntity() {
        return entityDef != null;
    }

    public boolean isAggregated() {
        return entityDef != null && entityDef.getRepository() != Object.class;
    }

    public void initialize() {
        if (entityDef != null && pkProxy == null) {
            doInitialize();
        }
    }

    public String toAlias(String property) {
        return aliasMap.get(property);
    }

    public List<String> toAliases(List<String> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<String> columns = new ArrayList<>(properties.size());
            for (String property : properties) {
                String alias = toAlias(property);
                columns.add(alias);
            }
            return columns;
        }
        return properties;
    }

    public Map<String, String> newAliasPropMap() {
        Map<String, String> map = new LinkedHashMap<>();
        aliasMap.forEach((key, value) -> map.put(value, key));
        return map;
    }

    protected abstract void doInitialize();

    public abstract boolean isCollection();

    public abstract Class<?> getGenericType();

    public abstract EntityType getEntityType();

}
