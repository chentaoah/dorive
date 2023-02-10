package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.ConfiguredRepository;

import java.util.List;

public interface Selector {

    boolean isMatch(BoundedContext boundedContext, ConfiguredRepository repository);

    List<String> selectColumns(BoundedContext boundedContext, ConfiguredRepository repository);

}
