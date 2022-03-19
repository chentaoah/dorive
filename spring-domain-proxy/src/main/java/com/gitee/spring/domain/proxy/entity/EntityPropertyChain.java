package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.impl.EntityPropertyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntityPropertyChain implements EntityProperty {
    private Class<?> lastEntityClass;
    private Class<?> entityClass;
    private String accessPath;
    private String fieldName;
    private EntityProperty lastEntityProperty;
    private EntityProperty entityProperty;

    public void initialize() {
        if (entityProperty == null) {
            entityProperty = EntityPropertyFactory.newEntityProperty(lastEntityClass, entityClass, fieldName);
            if (lastEntityProperty instanceof EntityPropertyChain) {
                ((EntityPropertyChain) lastEntityProperty).initialize();
            }
        }
    }

    @Override
    public Object getValue(Object entity) {
        if (lastEntityProperty != null) {
            entity = lastEntityProperty.getValue(entity);
        }
        return entity != null ? entityProperty.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object property) {
        if (lastEntityProperty != null) {
            entity = lastEntityProperty.getValue(entity);
        }
        if (entity != null) {
            entityProperty.setValue(entity, property);
        }
    }

}
