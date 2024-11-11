package com.gitee.dorive.web.entity;

import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EntityQueryConfig {
    private Class<?> entityClass;
    private AbstractQueryRepository<?, ?> repository;
    private Selector selector;
    private Class<?> queryClass;
    private Map<String, List<String>> classPropertiesMap;
}
