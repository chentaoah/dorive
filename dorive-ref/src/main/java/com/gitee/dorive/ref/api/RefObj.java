package com.gitee.dorive.ref.api;

import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;

public interface RefObj {

    int select(BoundedContext boundedContext);

    int select(Selector selector);

    int insertOrUpdate(BoundedContext boundedContext);

    int insertOrUpdate(Selector selector);

    int delete(BoundedContext boundedContext);

    int delete(Selector selector);

}
