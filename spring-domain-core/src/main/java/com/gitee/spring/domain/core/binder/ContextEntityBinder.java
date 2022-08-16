package com.gitee.spring.domain.core.binder;

import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;

public class ContextEntityBinder extends AbstractEntityBuilder {

    public ContextEntityBinder(BindingDefinition bindingDefinition) {
        super(bindingDefinition);
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        String bindAttribute = bindingDefinition.getBindAttribute();
        return boundedContext.get(bindAttribute);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        // ignore
    }

}
