package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.impl.observe.ObservedResult;
import com.gitee.dorive.core.repository.CommonRepository;

public interface Observed {

    ObservedResult accept(CommonRepository repository, BoundedContext boundedContext, Object entity);

}
