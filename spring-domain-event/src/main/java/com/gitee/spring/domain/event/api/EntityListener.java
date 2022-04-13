package com.gitee.spring.domain.event.api;

import com.gitee.spring.domain.event.entity.EntityEvent;

public interface EntityListener {

    void onEntityEvent(EntityEvent entityEvent);

}
