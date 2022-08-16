package com.gitee.spring.domain.core.binder;

import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;

public class PropertyEntityBinder extends AbstractEntityBuilder {

    public PropertyEntityBinder(BindingDefinition bindingDefinition) {
        super(bindingDefinition);
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
        Object boundValue = boundEntityPropertyChain.getValue(rootEntity);
        if (boundValue != null) {
            PropertyConverter propertyConverter = bindingDefinition.getPropertyConverter();
            boundValue = propertyConverter.convert(boundedContext, boundValue);
        }
        return boundValue;
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
        boundEntityPropertyChain.setValue(rootEntity, property);
    }

}
