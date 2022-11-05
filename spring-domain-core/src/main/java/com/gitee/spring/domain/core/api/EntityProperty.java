package com.gitee.spring.domain.core.api;

public interface EntityProperty {

    Object getValue(Object entity);

    void setValue(Object entity, Object property);

}
