package com.gitee.spring.domain.coating.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Example;

public interface ExampleBuilder {

    Example buildExample(BoundedContext boundedContext, Object coatingObject);

}
