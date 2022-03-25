package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.entity.*;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

public abstract class AbstractGenericRepository<E, PK> extends AbstractRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        E rootEntity = null;
        if (rootEntityDefinition != null) {
            Map<String, Object> queryParams = getQueryParamsFromContext(rootEntityDefinition, boundedContext, null);
            queryParams = queryParams == null ? new LinkedHashMap<>(2) : queryParams;
            queryParams.put("id", primaryKey);
            Object persistentObject = doSelectByQueryParams(rootEntityDefinition.getMapper(), boundedContext, false, queryParams);
            if (persistentObject != null) {
                EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
                rootEntity = (E) entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
                if (rootEntity != null) {
                    addFieldsToContext(rootEntityDefinition, boundedContext, rootEntity);
                }
            }
        } else {
            rootEntity = (E) ReflectUtils.newInstance(constructor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
            return rootEntity;
        }
        return null;
    }

    protected Map<String, Object> getQueryParamsFromContext(EntityDefinition entityDefinition, BoundedContext boundedContext, Object rootEntity) {
        EntityAttributes entityAttributes = entityDefinition.getEntityAttributes();
        List<AnnotationAttributes> annotationAttributes = entityAttributes.getObtainsAttributes();
        if (!annotationAttributes.isEmpty()) {
            Map<String, Object> queryParams = new LinkedHashMap<>();
            for (AnnotationAttributes attributes : annotationAttributes) {
                String context = attributes.getString(CONTEXT_ATTRIBUTES);
                String field = attributes.getString(FIELD_ATTRIBUTES);
                Object contextValue;
                if (rootEntity != null && context.startsWith("/")) {
                    EntityPropertyChain entityProperty = entityPropertyChainMap.get(context);
                    contextValue = entityProperty.getValue(rootEntity);
                } else {
                    contextValue = boundedContext.get(context);
                }
                queryParams.put(field, contextValue);
            }
            return queryParams;
        }
        return null;
    }

    protected void addFieldsToContext(EntityDefinition entityDefinition, BoundedContext boundedContext, Object entity) {
        EntityAttributes entityAttributes = entityDefinition.getEntityAttributes();
        List<AnnotationAttributes> annotationAttributes = entityAttributes.getJoinsAttributes();
        if (!annotationAttributes.isEmpty()) {
            for (AnnotationAttributes attributes : annotationAttributes) {
                String field = attributes.getString(FIELD_ATTRIBUTES);
                String context = attributes.getString(CONTEXT_ATTRIBUTES);
                Object fieldValue = BeanUtil.getFieldValue(entity, field);
                boundedContext.put(context, fieldValue);
            }
        }
    }

    protected void handleRootEntity(BoundedContext boundedContext, E rootEntity) {
        if (rootEntity instanceof RepositoryContext) {
            RepositoryContext repositoryContext = (RepositoryContext) rootEntity;
            repositoryContext.setRepository(this);
            repositoryContext.setBoundedContext(boundedContext);
        }
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityProperty();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null) {
                AnnotationAttributes attributes = entityDefinition.getEntityAttributes();
                boolean isIgnore = false;
                String[] ignoredOnStrs = attributes.getStringArray(IGNORED_ON_ATTRIBUTES);
                for (String ignoredOn : ignoredOnStrs) {
                    if (boundedContext.containsKey(ignoredOn)) {
                        isIgnore = true;
                        break;
                    }
                }
                if (isIgnore) {
                    continue;
                }
                Map<String, Object> queryParams = getQueryParamsFromContext(entityDefinition, boundedContext, rootEntity);
                if (queryParams != null) {
                    Object persistentObject = doSelectByQueryParams(entityDefinition.getMapper(), boundedContext,
                            attributes.getBoolean(MANY_TO_ONE_ATTRIBUTES), queryParams);
                    if (persistentObject != null) {
                        EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                        Object entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObject);
                        if (entity != null) {
                            EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                            entityProperty.setValue(lastEntity, entity);
                            addFieldsToContext(entityDefinition, boundedContext, entity);
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> findByExample(BoundedContext boundedContext, Object example) {
        Assert.notNull(rootEntityDefinition, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        Object persistentObject = doSelectByExample(rootEntityDefinition.getMapper(), boundedContext, example);
        if (persistentObject != null) {
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            List<E> rootEntities = (List<E>) entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
            for (E rootEntity : rootEntities) {
                handleRootEntity(boundedContext, rootEntity);
            }
            return rootEntities;
        }
        return Collections.emptyList();
    }

    @Override
    public void insert(BoundedContext boundedContext, E entity) {
        handleEntities(boundedContext, entity, (entityDefinition, targetEntity, persistentObject) -> {
            doInsert(entityDefinition.getMapper(), boundedContext, persistentObject);
        });
    }

    @Override
    public void update(BoundedContext boundedContext, E entity) {
        handleEntities(boundedContext, entity, (entityDefinition, targetEntity, persistentObject) -> {
            doUpdate(entityDefinition.getMapper(), boundedContext, persistentObject);
        });
    }

    @Override
    public void delete(BoundedContext boundedContext, E entity) {
        handleEntities(boundedContext, entity, (entityDefinition, targetEntity, persistentObject) -> {
            doDelete(entityDefinition.getMapper(), boundedContext, persistentObject);
        });
    }

    protected void handleEntities(BoundedContext boundedContext, E entity, Consumer consumer) {
        Assert.notNull(entity, "The entity cannot be null!");
        for (EntityDefinition entityDefinition : orderedEntityDefinitions) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                Object persistentObject = entityAssembler.disassemble(boundedContext, entity, entityDefinition, targetEntity);
                if (persistentObject != null) {
                    consumer.accept(entityDefinition, targetEntity, persistentObject);
                }
            }
        }
    }

    protected abstract Object doSelectByQueryParams(Object mapper, BoundedContext boundedContext, boolean manyToOne, Map<String, Object> queryParams);

    protected abstract Object doSelectByExample(Object mapper, BoundedContext boundedContext, Object example);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDelete(Object mapper, BoundedContext boundedContext, Object persistentObject);

    public interface Consumer {
        void accept(EntityDefinition entityDefinition, Object entity, Object persistentObject);
    }

}
