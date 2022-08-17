package com.gitee.spring.domain.core.binder;

import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyEntityBinder extends AbstractEntityBuilder {

    protected String belongAccessPath;
    protected ConfiguredRepository belongConfiguredRepository;
    protected EntityPropertyChain boundEntityPropertyChain;
    protected PropertyConverter propertyConverter;

    public PropertyEntityBinder(BindingDefinition bindingDefinition,
                                EntityPropertyChain fieldEntityPropertyChain,
                                String belongAccessPath,
                                ConfiguredRepository belongConfiguredRepository,
                                EntityPropertyChain boundEntityPropertyChain,
                                PropertyConverter propertyConverter) {
        super(bindingDefinition, fieldEntityPropertyChain);
        this.belongAccessPath = belongAccessPath;
        this.belongConfiguredRepository = belongConfiguredRepository;
        this.boundEntityPropertyChain = boundEntityPropertyChain;
        this.propertyConverter = propertyConverter;
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        Object boundValue = boundEntityPropertyChain.getValue(rootEntity);
        if (boundValue != null) {
            boundValue = propertyConverter.convert(boundedContext, boundValue);
        }
        return boundValue;
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        boundEntityPropertyChain.setValue(rootEntity, property);
    }

}
