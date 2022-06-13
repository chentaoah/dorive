package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityCache;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BatchRepository extends ConfiguredRepository {

    public BatchRepository(ConfiguredRepository configuredRepository) {
        super(configuredRepository);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        EntityCache entityCache = boundedContext.getEntityCache();
        if (entityCache != null && example instanceof EntityExample) {
            Class<?> repositoryClass = abstractContextRepository.getRepositoryClass();
            String accessPath = entityDefinition.getAccessPath();
            Map<String, List<Object>> entitiesMap = entityCache.getCache(repositoryClass, accessPath);
            if (entitiesMap != null) {
                if (entitiesMap.isEmpty()) {
                    return Collections.emptyList();
                }
                EntityExample entityExample = (EntityExample) example;
                StringBuilder builder = new StringBuilder();
                for (EntityCriterion entityCriterion : entityExample.getEntityCriteria()) {
                    String fieldName = entityCriterion.getFieldName();
                    Object fieldValue = entityCriterion.getFieldValue();
                    builder.append(fieldName).append(": ").append(fieldValue).append(", ");
                }
                if (builder.length() > 0) {
                    builder.delete(builder.length() - 2, builder.length());
                }
                List<Object> entities = entitiesMap.get(builder.toString());
                return entities != null ? entities : Collections.emptyList();
            }
        }
        return super.selectByExample(boundedContext, example);
    }

}
