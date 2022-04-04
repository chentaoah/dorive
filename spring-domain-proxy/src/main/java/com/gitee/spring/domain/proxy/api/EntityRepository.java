package com.gitee.spring.domain.proxy.api;

public interface EntityRepository {

    void updateByExample(Object entity, Object example);

    void deleteByExample(Class<?> entityClass, Object example);

}
