package com.gitee.spring.domain.coating.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DefaultCoatingAssembler implements CoatingAssembler {

    private CoatingDefinition coatingDefinition;
    private List<PropertyDefinition> availablePropertyDefinitions;
    private List<RepositoryLocation> reversedRepositoryLocations;

    @Override
    public void assemble(Object coatingObject, Object entity) {
        for (PropertyDefinition propertyDefinition : availablePropertyDefinitions) {
            EntityPropertyChain entityPropertyChain = propertyDefinition.getEntityPropertyChain();
            Object targetValue = entityPropertyChain.getValue(entity);
            ReflectUtil.setFieldValue(coatingObject, propertyDefinition.getDeclaredField(), targetValue);
        }
    }

    @Override
    public void disassemble(Object coatingObject, Object entity) {
        for (PropertyDefinition propertyDefinition : availablePropertyDefinitions) {
            EntityPropertyChain entityPropertyChain = propertyDefinition.getEntityPropertyChain();
            Object fieldValue = ReflectUtil.getFieldValue(coatingObject, propertyDefinition.getDeclaredField());
            entityPropertyChain.setValue(entity, fieldValue);
        }
    }

}
