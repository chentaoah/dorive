package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;

public interface EntityFactory {

    Object reconstitute(BoundedContext boundedContext, Object persistentObject);

}
