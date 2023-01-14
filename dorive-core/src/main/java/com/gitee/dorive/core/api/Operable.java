package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.impl.operable.OperationResult;
import com.gitee.dorive.core.repository.ConfiguredRepository;

public interface Operable {

    OperationResult accept(ConfiguredRepository repository, BoundedContext boundedContext, Object entity);

}
