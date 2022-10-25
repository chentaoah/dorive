package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;

public class ContextBinder extends AbstractBinder {

    public ContextBinder(BindingDefinition bindingDefinition, EntityPropertyChain fieldProperty) {
        super(bindingDefinition, fieldProperty);
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
