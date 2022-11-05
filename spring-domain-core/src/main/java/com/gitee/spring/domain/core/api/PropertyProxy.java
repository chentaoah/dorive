package com.gitee.spring.domain.core.api;

public interface PropertyProxy {

    Object getValue(Object entity);

    void setValue(Object entity, Object property);

}
