package com.gitee.spring.domain.core.binder;

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

    @Override
    public String getColumnName() {
        return bindingDefinition.getAliasAttribute();
    }

    @Override
    public Object getFieldValue(BoundedContext boundedContext, Object entity) {
        EntityPropertyChain fieldEntityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
        return fieldEntityPropertyChain.getValue(entity);
    }

    @Override
    public void setFieldValue(BoundedContext boundedContext, Object entity, Object property) {
        EntityPropertyChain fieldEntityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
        fieldEntityPropertyChain.setValue(entity, property);
    }

}
