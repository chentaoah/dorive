package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.EntityEvent;

public interface EntityListener {

    void onEntityEvent(EntityEvent entityEvent);

}
