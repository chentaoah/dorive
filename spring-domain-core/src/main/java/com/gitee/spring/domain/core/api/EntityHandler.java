package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public interface EntityHandler {

    void handleEntities(BoundedContext boundedContext, List<Object> rootEntities);

}
