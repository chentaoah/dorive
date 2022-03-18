package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

public interface EntityAssembler {

    Object assemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object primaryKey);

    Object disassemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity);

}
