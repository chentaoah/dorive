package com.gitee.spring.domain.coating.property;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultCoatingAssembler implements CoatingAssembler {

    private CoatingDefinition coatingDefinition;

    @Override
    public void assemble(Object coating, Object entity) {
        for (PropertyDefinition propertyDefinition : coatingDefinition.getPropertyDefinitions()) {
            EntityPropertyLocation entityPropertyLocation = propertyDefinition.getEntityPropertyLocation();
            if ("".equals(entityPropertyLocation.getPrefixAccessPath())) {
                EntityPropertyChain entityPropertyChain = entityPropertyLocation.getEntityPropertyChain();
                Object targetValue = entityPropertyChain.getValue(entity);
                ReflectUtil.setFieldValue(coating, propertyDefinition.getField(), targetValue);
            }
        }
    }

    @Override
    public void disassemble(Object coating, Object entity) {
        for (PropertyDefinition propertyDefinition : coatingDefinition.getPropertyDefinitions()) {
            EntityPropertyLocation entityPropertyLocation = propertyDefinition.getEntityPropertyLocation();
            if ("".equals(entityPropertyLocation.getPrefixAccessPath())) {
                Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getField());
                EntityPropertyChain entityPropertyChain = entityPropertyLocation.getEntityPropertyChain();
                entityPropertyChain.setValue(entity, fieldValue);
            }
        }
    }

}
