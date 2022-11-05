package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;

import java.util.List;

public interface EntityHandler {

    void handleEntities(BoundedContext boundedContext, List<Object> rootEntities);

}
