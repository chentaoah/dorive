package com.gitee.spring.domain.proxy.api;

public interface EntityProperty {

    Object getValue(Object entity);

    void setValue(Object entity, Object property);

}
