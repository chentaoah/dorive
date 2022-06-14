package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityCaches;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatchRepository extends ConfiguredRepository {

    public BatchRepository(ConfiguredRepository configuredRepository) {
        super(configuredRepository);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        EntityCaches entityCaches = boundedContext.getEntityCaches();
        if (entityCaches != null && example instanceof EntityExample) {
            Class<?> repositoryClass = abstractContextRepository.getRepositoryClass();
            String accessPath = entityDefinition.getAccessPath();
            Map<String, List<Object>> entitiesMap = entityCaches.getCache(repositoryClass, accessPath);
            if (entitiesMap != null) {
                if (entitiesMap.isEmpty()) {
                    return Collections.emptyList();
                }
                Set<String> entityJoinAliases = entityDefinition.getEntityJoinAliases();
                EntityExample entityExample = (EntityExample) example;
                StringBuilder builder = new StringBuilder();
                for (EntityCriterion entityCriterion : entityExample.getEntityCriteria()) {
                    String fieldName = entityCriterion.getFieldName();
                    if (entityJoinAliases.contains(fieldName)) {
                        Object fieldValue = entityCriterion.getFieldValue();
                        builder.append(fieldName).append(": ").append(fieldValue).append(", ");
                    }
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
