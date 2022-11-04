package com.gitee.spring.domain.coating3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;

public interface ExampleBuilder {

    Example buildExample(BoundedContext boundedContext, Object coatingObject);

}
