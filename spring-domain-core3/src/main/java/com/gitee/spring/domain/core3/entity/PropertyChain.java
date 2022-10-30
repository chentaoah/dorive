package com.gitee.spring.domain.core3.entity;

import com.gitee.spring.domain.common.api.EntityProperty;
import com.gitee.spring.domain.common.impl.EntityPropertyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
@ToString(exclude = "lastPropertyChain")
public class PropertyChain implements EntityProperty {

    private PropertyChain lastPropertyChain;
    private Class<?> lastEntityClass;
    private String accessPath;
    private Field declaredField;
    private boolean annotatedEntity;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private EntityProperty entityProperty;

    public PropertyChain(PropertyChain lastPropertyChain,
                         PropertyChain propertyChain) {
        this.lastPropertyChain = lastPropertyChain;
        this.lastEntityClass = propertyChain.getLastEntityClass();
        this.accessPath = propertyChain.getAccessPath();
        this.declaredField = propertyChain.getDeclaredField();
        this.annotatedEntity = propertyChain.isAnnotatedEntity();
        this.entityClass = propertyChain.getEntityClass();
        this.collection = propertyChain.isCollection();
        this.genericEntityClass = propertyChain.getGenericEntityClass();
        this.fieldName = propertyChain.getFieldName();
        this.entityProperty = propertyChain.getEntityProperty();
    }

    public void initialize() {
        if (entityProperty == null) {
            entityProperty = EntityPropertyFactory.newEntityProperty(lastEntityClass, entityClass, fieldName);
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
