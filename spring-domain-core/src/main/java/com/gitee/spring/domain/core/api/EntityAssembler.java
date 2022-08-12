package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

public interface EntityAssembler {

    Object assemble(BoundedContext boundedContext, Object persistentObject);

    Object disassemble(BoundedContext boundedContext, Object entity);

}
