package com.gitee.spring.domain.proxy.extension;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

public class DefaultEntityAssembler implements EntityAssembler {

    @Override
    public Object assemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object persistentObject) {
        return BeanUtil.copyProperties(persistentObject, entityDefinition.getGenericEntityClass());
    }

    @Override
    public Object disassemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object entity) {
        return BeanUtil.copyProperties(entity, entityDefinition.getPojoClass());
    }

}
