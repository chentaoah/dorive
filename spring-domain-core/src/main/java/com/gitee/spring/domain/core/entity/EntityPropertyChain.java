package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.impl.EntityPropertyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
@ToString(exclude = "lastEntityPropertyChain")
public class EntityPropertyChain implements EntityProperty {

    private EntityPropertyChain lastEntityPropertyChain;
    private Class<?> lastEntityClass;
    private String accessPath;
    private Field declaredField;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private EntityProperty entityProperty;

    public EntityPropertyChain(EntityPropertyChain lastEntityPropertyChain,
                               EntityPropertyChain entityPropertyChain) {
        this.lastEntityPropertyChain = lastEntityPropertyChain;
        this.lastEntityClass = entityPropertyChain.getLastEntityClass();
        this.accessPath = entityPropertyChain.getAccessPath();
        this.declaredField = entityPropertyChain.getDeclaredField();
        this.entityClass = entityPropertyChain.getEntityClass();
        this.collection = entityPropertyChain.isCollection();
        this.genericEntityClass = entityPropertyChain.getGenericEntityClass();
        this.fieldName = entityPropertyChain.getFieldName();
        this.entityProperty = entityPropertyChain.getEntityProperty();
    }

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
