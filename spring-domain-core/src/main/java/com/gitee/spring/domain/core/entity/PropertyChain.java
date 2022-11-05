package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.PropertyProxy;
import com.gitee.spring.domain.core.impl.PropertyProxyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(exclude = "lastPropertyChain")
public class PropertyChain implements PropertyProxy {

    private PropertyChain lastPropertyChain;
    private Class<?> lastEntityClass;
    private Property property;
    private String accessPath;
    private boolean annotatedEntity;
    private PropertyProxy propertyProxy;

    public PropertyChain(PropertyChain lastPropertyChain,
                         PropertyChain propertyChain) {
        this.lastPropertyChain = lastPropertyChain;
        this.lastEntityClass = propertyChain.getLastEntityClass();
        this.property = propertyChain.getProperty();
        this.accessPath = propertyChain.getAccessPath();
        this.annotatedEntity = propertyChain.isAnnotatedEntity();
        this.propertyProxy = propertyChain.getPropertyProxy();
    }

    public void initialize() {
        if (propertyProxy == null) {
            propertyProxy = PropertyProxyFactory.newPropertyProxy(lastEntityClass, property.getFieldClass(), property.getFieldName());
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
        return entity != null ? propertyProxy.getValue(entity) : null;
    }

    @Override
    public void setValue(Object entity, Object property) {
        if (lastPropertyChain != null) {
            entity = lastPropertyChain.getValue(entity);
        }
        if (entity != null) {
            propertyProxy.setValue(entity, property);
        }
    }

}
