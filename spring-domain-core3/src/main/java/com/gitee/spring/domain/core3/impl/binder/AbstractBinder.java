package com.gitee.spring.domain.core3.impl.binder;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractBinder implements Binder {

    protected BindingDefinition bindingDefinition;
    protected EntityPropertyChain fieldEntityPropertyChain;

    @Override
    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    @Override
    public Object getFieldValue(BoundedContext boundedContext, Object entity) {
        return fieldEntityPropertyChain.getValue(entity);
    }

    @Override
    public void setFieldValue(BoundedContext boundedContext, Object entity, Object property) {
        fieldEntityPropertyChain.setValue(entity, property);
    }

}
