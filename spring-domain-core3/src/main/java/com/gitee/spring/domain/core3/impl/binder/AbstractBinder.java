package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractBinder implements Binder {

    protected BindingDefinition bindingDefinition;
    protected PropertyChain fieldPropertyChain;

    @Override
    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    @Override
    public Object getFieldValue(BoundedContext boundedContext, Object entity) {
        return fieldPropertyChain.getValue(entity);
    }

    @Override
    public void setFieldValue(BoundedContext boundedContext, Object entity, Object property) {
        fieldPropertyChain.setValue(entity, property);
    }

}
