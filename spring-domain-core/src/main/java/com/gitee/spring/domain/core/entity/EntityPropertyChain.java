package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.property.EntityPropertyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(exclude = "lastEntityPropertyChain")
public class EntityPropertyChain implements EntityProperty {
    private EntityPropertyChain lastEntityPropertyChain;
    private String accessPath;
    private Class<?> lastEntityClass;
    private Class<?> entityClass;
    private String fieldName;
    private EntityProperty entityProperty;
    private boolean boundProperty;

    public void initialize() {
        if (entityProperty == null) {
            entityProperty = EntityPropertyFactory.newEntityProperty(lastEntityClass, entityClass, fieldName);
            if (lastEntityPropertyChain != null) {
                lastEntityPropertyChain.initialize();
            }
        }
    }

    @Override
    public Object getValue(Object entity) {
        if (lastEntityPropertyChain != null) {
            entity = lastEntityPropertyChain.getValue(entity);
        }
        return entity != null ? entityProperty.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object property) {
        if (lastEntityPropertyChain != null) {
            entity = lastEntityPropertyChain.getValue(entity);
        }
        if (entity != null) {
            entityProperty.setValue(entity, property);
        }
    }

}
