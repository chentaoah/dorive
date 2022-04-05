package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

public interface EntityAssembler {

    Object assemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object persistentObject);

    Object disassemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object entity);

}
