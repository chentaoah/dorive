package com.gitee.dorive.base.v1.event.api;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;

public interface EventPublisher {

    void publishEvent(Class<?> entityClass, Context context, EntityOp entityOp);

}
