package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.repository.BindRepository;

public class PropertyBinder extends AbstractBinder {

    protected String belongAccessPath;
    protected BindRepository belongBindRepository;
    protected EntityPropertyChain boundEntityPropertyChain;

    public PropertyBinder(BindingDefinition bindingDefinition,
                          EntityPropertyChain fieldEntityPropertyChain,
                          String belongAccessPath,
                          BindRepository belongBindRepository,
                          EntityPropertyChain boundEntityPropertyChain) {
        super(bindingDefinition, fieldEntityPropertyChain);
        this.belongAccessPath = belongAccessPath;
        this.belongBindRepository = belongBindRepository;
        this.boundEntityPropertyChain = boundEntityPropertyChain;
    }

    @Override
    public Object getBoundValue(BoundedContext boundedContext, Object rootEntity) {
        return boundEntityPropertyChain.getValue(rootEntity);
    }

    @Override
    public void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property) {
        boundEntityPropertyChain.setValue(rootEntity, property);
    }

}
