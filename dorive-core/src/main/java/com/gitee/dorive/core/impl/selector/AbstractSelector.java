package com.gitee.dorive.core.impl.selector;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;

public abstract class AbstractSelector implements Selector {

    @Override
    public Context build() {
        return new BoundedContext(this);
    }

}
