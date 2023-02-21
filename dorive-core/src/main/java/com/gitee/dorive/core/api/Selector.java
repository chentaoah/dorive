package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.CommonRepository;

import java.util.List;

public interface Selector {

    boolean isMatch(BoundedContext boundedContext, CommonRepository repository);

    boolean isRelay(BoundedContext boundedContext, CommonRepository repository);

    List<String> selectColumns(BoundedContext boundedContext, CommonRepository repository);

}
