package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;

public interface EntityAssembler {

    Object assemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object persistentObject);

    Object disassemble(EntityDefinition entityDefinition, BoundedContext boundedContext, Object entity);

}
