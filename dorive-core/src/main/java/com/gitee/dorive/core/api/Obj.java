package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;

public interface Obj {

    int select(BoundedContext boundedContext);

    int select(Selector selector);

}
