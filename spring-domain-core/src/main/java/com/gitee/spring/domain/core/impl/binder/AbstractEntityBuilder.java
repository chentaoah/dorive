package com.gitee.spring.domain.core.impl.binder;

import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractEntityBuilder implements EntityBinder {

    protected BindingDefinition bindingDefinition;
    protected EntityPropertyChain fieldEntityPropertyChain;

    @Override
    public String getColumnName() {
        return bindingDefinition.getAliasAttribute();
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
