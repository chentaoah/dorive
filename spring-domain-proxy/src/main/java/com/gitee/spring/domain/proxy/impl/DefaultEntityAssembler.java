package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

public class DefaultEntityAssembler implements EntityAssembler {

    @Override
    public Object assemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object persistentObject) {
        return BeanUtil.copyProperties(persistentObject, entityDefinition.getGenericEntityClass());
    }

    @Override
    public Object disassemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        return BeanUtil.copyProperties(entity, entityDefinition.getPojoClass());
    }

}
