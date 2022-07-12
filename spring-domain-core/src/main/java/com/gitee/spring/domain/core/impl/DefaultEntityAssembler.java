package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;

public class DefaultEntityAssembler implements EntityAssembler {

    @Override
    public Object assemble(BoundedContext boundedContext, EntityDefinition entityDefinition, Object persistentObject) {
        if (entityDefinition.isSameType()) {
            return persistentObject;
        } else {
            return BeanUtil.copyProperties(persistentObject, entityDefinition.getGenericEntityClass());
        }
    }

    @Override
    public Object disassemble(BoundedContext boundedContext, EntityDefinition entityDefinition, Object entity) {
        if (entityDefinition.isSameType()) {
            return entity;
        } else {
            return BeanUtil.copyProperties(entity, entityDefinition.getMappedClass());
        }
    }

}
