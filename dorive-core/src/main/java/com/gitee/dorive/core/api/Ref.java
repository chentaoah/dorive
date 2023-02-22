package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;

public interface Ref {

    int select(BoundedContext boundedContext);

    int select(Selector selector);

    int insertOrUpdate(BoundedContext boundedContext);

    int insertOrUpdate(Selector selector);

    int delete(BoundedContext boundedContext);

    int delete(Selector selector);

}
