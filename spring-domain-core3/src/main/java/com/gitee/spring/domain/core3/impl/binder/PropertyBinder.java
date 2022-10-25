package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

public class PropertyBinder extends AbstractBinder {

    protected String belongAccessPath;
    protected ConfiguredRepository belongRepository;
    protected EntityPropertyChain boundProperty;

    public PropertyBinder(BindingDefinition bindingDefinition,
                          EntityPropertyChain fieldProperty,
                          String belongAccessPath,
                          ConfiguredRepository belongRepository,
                          EntityPropertyChain boundProperty) {
        super(bindingDefinition, fieldProperty);
        this.belongAccessPath = belongAccessPath;
        this.belongRepository = belongRepository;
        this.boundProperty = boundProperty;
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        return boundProperty.getValue(rootEntity);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        boundProperty.setValue(rootEntity, property);
    }

}
