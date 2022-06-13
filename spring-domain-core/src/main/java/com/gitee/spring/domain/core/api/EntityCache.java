package com.gitee.spring.domain.core.api;

import java.util.List;
import java.util.Map;

public interface EntityCache {

    Map<String, List<Object>> getOrCreateCache(Class<?> repositoryClass, String accessPath);

    Map<String, List<Object>> getCache(Class<?> repositoryClass, String accessPath);

}
