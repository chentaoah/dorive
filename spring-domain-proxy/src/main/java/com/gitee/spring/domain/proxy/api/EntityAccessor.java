package com.gitee.spring.domain.proxy.api;

public interface EntityAccessor {

    void setValue(Object rootEntity, Object entity);

    Object getValue(Object rootEntity);

}
