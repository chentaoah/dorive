package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.QueryCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultQueryCache implements QueryCache {

    protected Map<Class<?>, Map<String, List<Object>>> entitiesCache = new ConcurrentHashMap<>();

    @Override
    public Object selectByPrimaryKey(Class<?> entityClass, String primaryKey) {
        Map<String, List<Object>> entitiesMap = entitiesCache.get(entityClass);
        return entitiesMap.get(primaryKey);
    }

}
