package com.gitee.dorive.api.impl;

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.ele.FieldElement;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropChain implements PropProxy {

    private FieldElement fieldElement;
    private PropProxy propProxy;

    @Override
    public Object getValue(Object entity) {
        return propProxy.getValue(entity);
    }

    @Override
    public void setValue(Object entity, Object value) {
        propProxy.setValue(entity, value);
    }

    public boolean isSameType(PropChain boundPropChain) {
        return fieldElement.isSameType(boundPropChain.getFieldElement());
    }

}
