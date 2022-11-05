package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

public interface EntityFactory {

    Object reconstitute(BoundedContext boundedContext, Object persistentObject);

    Object deconstruct(BoundedContext boundedContext, Object entity);

}
