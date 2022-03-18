package com.gitee.spring.domain.proxy.api;

public interface EntityProperty {

    void setValue(Object entity, Object property);

    Object getValue(Object entity);

}
