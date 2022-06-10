package com.gitee.spring.domain.core.api;

public interface QueryCache {

    Object selectByPrimaryKey(Class<?> entityClass, String primaryKey);

}
