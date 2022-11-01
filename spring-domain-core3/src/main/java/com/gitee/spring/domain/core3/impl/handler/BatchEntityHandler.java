package com.gitee.spring.domain.core3.impl.handler;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.common.api.EntityProperty;
import com.gitee.spring.domain.common.util.ContextUtils;
import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.api.EntityIndex;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.executor.Fishhook;
import com.gitee.spring.domain.core3.entity.executor.Page;
import com.gitee.spring.domain.core3.entity.executor.UnionExample;
import com.gitee.spring.domain.core3.impl.DefaultEntityIndex;
import com.gitee.spring.domain.core3.impl.binder.ContextBinder;
import com.gitee.spring.domain.core3.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        for (ConfiguredRepository subRepository : repository.getSubRepositories()) {
            if (subRepository.matchContext(boundedContext)) {
                PropertyChain anchorPoint = subRepository.getAnchorPoint();
                PropertyChain lastPropertyChain = anchorPoint.getLastPropertyChain();

                String pageKey = subRepository.getEntityDefinition().getPageKey();
                Page<Object> page = StringUtils.isNotBlank(pageKey) ? (Page<Object>) boundedContext.get(pageKey) : null;

                UnionExample unionExample = new UnionExample();
                for (Object rootEntity : rootEntities) {
                    Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
                    if (lastEntity != null) {
                        Example example = newExampleByContext(subRepository, boundedContext, rootEntity);
                        Object primaryKey = BeanUtil.getFieldValue(rootEntity, "id");
                        example.selectColumns(primaryKey + " as $id");
                        example.setPage(page);
                        unionExample.mergeExample(example);
                    }
                }

                Fishhook fishhook = new Fishhook(null);
                boundedContext.put("#fishhook", fishhook);
                List<Object> allEntities = subRepository.selectByExample(boundedContext, unionExample);
                boundedContext.remove("#fishhook");

                EntityIndex entityIndex = newEntityIndex(rootEntities, fishhook.getSource(), allEntities);
                for (Object rootEntity : rootEntities) {
                    Object lastEntity = lastPropertyChain == null ? rootEntity : lastPropertyChain.getValue(rootEntity);
                    if (lastEntity != null) {
                        List<Object> entities = entityIndex.selectList(rootEntity);
                        Object entity = convertManyToOneEntity(subRepository, entities);
                        if (entity != null) {
                            EntityProperty entityProperty = anchorPoint.getEntityProperty();
                            entityProperty.setValue(lastEntity, entity);
                        }
                    }
                }
            }
        }
    }

    private Example newExampleByContext(ConfiguredRepository repository, BoundedContext boundedContext, Object rootEntity) {
        Example example = new Example();
        for (PropertyBinder propertyBinder : repository.getBinderResolver().getPropertyBinders()) {
            String alias = propertyBinder.getBindingDefinition().getAlias();
            Object boundValue = propertyBinder.getBoundValue(boundedContext, rootEntity);
            if (boundValue instanceof Collection) {
                boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
            }
            if (boundValue != null) {
                example.eq(alias, boundValue);
            } else {
                example.setEmptyQuery(true);
                break;
            }
        }
        if (!example.isEmptyQuery() && example.isDirtyQuery()) {
            newCriterionByContext(repository, boundedContext, rootEntity, example);
        }
        return example;
    }

    private void newCriterionByContext(ConfiguredRepository repository, BoundedContext boundedContext, Object rootEntity, Example example) {
        for (ContextBinder contextBinder : repository.getBinderResolver().getContextBinders()) {
            String alias = contextBinder.getBindingDefinition().getAlias();
            Object boundValue = contextBinder.getBoundValue(boundedContext, rootEntity);
            if (boundValue != null) {
                if (boundValue instanceof String && ContextUtils.isLike((String) boundValue)) {
                    boundValue = ContextUtils.stripLike((String) boundValue);
                    example.like(alias, boundValue);
                } else {
                    example.eq(alias, boundValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private EntityIndex newEntityIndex(List<Object> rootEntities, Object source, List<Object> entities) {
        if (source instanceof List) {
            return new DefaultEntityIndex(rootEntities, (List<Map<String, Object>>) source, entities);
        }
        throw new RuntimeException("Unsupported type!");
    }

    private Object convertManyToOneEntity(ConfiguredRepository repository, List<?> entities) {
        ElementDefinition elementDefinition = repository.getElementDefinition();
        if (elementDefinition.isCollection()) {
            return entities;
        } else if (!entities.isEmpty()) {
            return entities.get(0);
        }
        return null;
    }

}
