package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.EntityEvent;

public interface EntityListener {

    void onEntityEvent(EntityEvent entityEvent);

}
