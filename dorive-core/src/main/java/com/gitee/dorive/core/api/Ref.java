package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.BoundedContext;

public interface Ref {

    void relay(BoundedContext boundedContext);

    void relay(Selector selector);

}
