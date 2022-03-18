package com.gitee.spring.domain.proxy.api;

public interface EntityAccessor {

    boolean checkRouteNull(Object rootEntity);

    void setValue(Object rootEntity, Object entity);

    Object getValue(Object rootEntity);

}
