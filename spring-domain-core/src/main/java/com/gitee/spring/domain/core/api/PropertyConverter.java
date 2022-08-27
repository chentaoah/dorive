package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

public interface PropertyConverter {

    Object convert(BoundedContext boundedContext, Object property);

    Object reverseConvert(BoundedContext boundedContext, Object property);

}
