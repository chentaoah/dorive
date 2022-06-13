package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.QueryCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultQueryCache implements QueryCache {

    protected Map<Class<?>, Map<String, Map<String, List<Object>>>> entitiesCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, List<Object>> getOrCreateCache(Class<?> repositoryClass, String accessPath) {
        Map<String, Map<String, List<Object>>> entitiesMap = entitiesCache.computeIfAbsent(repositoryClass, key -> new ConcurrentHashMap<>());
        return entitiesMap.computeIfAbsent(accessPath, key -> new ConcurrentHashMap<>());
    }

    @Override
    public Map<String, List<Object>> getCache(Class<?> repositoryClass, String accessPath) {
        Map<String, Map<String, List<Object>>> entitiesMap = entitiesCache.get(repositoryClass);
        return entitiesMap != null ? entitiesMap.get(accessPath) : null;
    }

}
