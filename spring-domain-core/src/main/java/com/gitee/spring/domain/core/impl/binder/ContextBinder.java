package com.gitee.spring.domain.core.impl.binder;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;

public class ContextBinder extends AbstractBinder {

    public ContextBinder(BindingDefinition bindingDefinition, PropertyChain fieldPropertyChain) {
        super(bindingDefinition, fieldPropertyChain);
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        String bindCtx = bindingDefinition.getBindCtx();
        return boundedContext.get(bindCtx);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        // ignore
    }

}
