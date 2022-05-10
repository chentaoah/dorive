package com.gitee.spring.domain.coating.property;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DefaultCoatingAssembler implements CoatingAssembler {

    private CoatingDefinition coatingDefinition;
    private List<PropertyDefinition> availablePropertyDefinitions;

    @Override
    public void assemble(Object coating, Object entity) {
        for (PropertyDefinition propertyDefinition : availablePropertyDefinitions) {
            EntityPropertyChain entityPropertyChain = propertyDefinition.getEntityPropertyChain();
            Object targetValue = entityPropertyChain.getValue(entity);
            ReflectUtil.setFieldValue(coating, propertyDefinition.getField(), targetValue);
        }
    }

    @Override
    public void disassemble(Object coating, Object entity) {
        for (PropertyDefinition propertyDefinition : availablePropertyDefinitions) {
            EntityPropertyChain entityPropertyChain = propertyDefinition.getEntityPropertyChain();
            Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getField());
            entityPropertyChain.setValue(entity, fieldValue);
        }
    }

}
