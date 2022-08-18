package com.gitee.spring.domain.core.binder;

import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;

public class ContextEntityBinder extends AbstractEntityBuilder {

    public ContextEntityBinder(BindingDefinition bindingDefinition, EntityPropertyChain fieldEntityPropertyChain) {
        super(bindingDefinition, fieldEntityPropertyChain);
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
