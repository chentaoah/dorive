package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

public class PropertyBinder extends AbstractBinder {

    protected String belongAccessPath;
    protected ConfiguredRepository belongRepository;
    protected PropertyChain boundPropertyChain;

    public PropertyBinder(BindingDefinition bindingDefinition,
                          PropertyChain fieldPropertyChain,
                          String belongAccessPath,
                          ConfiguredRepository belongRepository,
                          PropertyChain boundPropertyChain) {
        super(bindingDefinition, fieldPropertyChain);
        this.belongAccessPath = belongAccessPath;
        this.belongRepository = belongRepository;
        this.boundPropertyChain = boundPropertyChain;
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        return boundPropertyChain.getValue(rootEntity);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        boundPropertyChain.setValue(rootEntity, property);
    }

}
