package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.impl.operable.FuncResult;
import com.gitee.dorive.core.repository.ConfiguredRepository;

public interface Operable {

    FuncResult accept(ConfiguredRepository repository, BoundedContext boundedContext, Object entity);

}
