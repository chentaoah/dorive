package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;

public interface EntityAssembler {

    Object assemble(BoundedContext boundedContext, EntityDefinition entityDefinition, Object persistentObject);

    Object disassemble(BoundedContext boundedContext, EntityDefinition entityDefinition, Object entity);

}
