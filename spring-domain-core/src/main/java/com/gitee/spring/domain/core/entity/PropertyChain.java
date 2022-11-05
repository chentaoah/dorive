package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.impl.EntityPropertyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(exclude = "lastPropertyChain")
public class PropertyChain implements EntityProperty {

    private PropertyChain lastPropertyChain;
    private Class<?> lastEntityClass;
    private Property property;
    private String accessPath;
    private boolean annotatedEntity;
    private EntityProperty entityProperty;

    public PropertyChain(PropertyChain lastPropertyChain,
                         PropertyChain propertyChain) {
        this.lastPropertyChain = lastPropertyChain;
        this.lastEntityClass = propertyChain.getLastEntityClass();
        this.property = propertyChain.getProperty();
        this.accessPath = propertyChain.getAccessPath();
        this.annotatedEntity = propertyChain.isAnnotatedEntity();
        this.entityProperty = propertyChain.getEntityProperty();
    }

    public void initialize() {
        if (entityProperty == null) {
            entityProperty = EntityPropertyFactory.newEntityProperty(lastEntityClass, property.getFieldClass(), property.getFieldName());
            if (lastPropertyChain != null) {
                lastPropertyChain.initialize();
            }
        }
    }

    @Override
    public Object getValue(Object entity) {
        if (lastPropertyChain != null) {
            entity = lastPropertyChain.getValue(entity);
        }
        return entity != null ? entityProperty.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object property) {
        if (lastPropertyChain != null) {
            entity = lastPropertyChain.getValue(entity);
        }
        if (entity != null) {
            entityProperty.setValue(entity, property);
        }
    }

}
