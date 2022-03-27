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
            Object persistentObject = doSelectByPrimaryKey(rootEntityDefinition.getMapper(), boundedContext, primaryKey);
            if (persistentObject != null) {
                EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
                rootEntity = (E) entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
            }
        } else {
            rootEntity = (E) ReflectUtils.newInstance(constructor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
        }
        return rootEntity;
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
                AnnotationAttributes attributes = entityDefinition.getAttributes();
                if (isIgnoreEntityDefinition(attributes, boundedContext)) {
                    continue;
                }
                Map<String, Object> queryParams = getQueryParamsFromContext(entityDefinition, boundedContext, rootEntity);
                Object persistentObject = doSelectByExample(entityDefinition.getMapper(), boundedContext,
                        attributes.getBoolean(MANY_TO_ONE_ATTRIBUTES), queryParams);
                if (persistentObject != null) {
                    if (entityDefinition.isCollection()) {
                        for (Object eachPersistentObject : (Collection<?>) persistentObject) {
                            handleSingleEntity(boundedContext, rootEntity, entityDefinition, entityPropertyChain, lastEntity, eachPersistentObject);
                        }
                    } else {
                        handleSingleEntity(boundedContext, rootEntity, entityDefinition, entityPropertyChain, lastEntity, persistentObject);
                    }
                }
            }
        }
    }

    protected boolean isIgnoreEntityDefinition(AnnotationAttributes attributes, BoundedContext boundedContext) {
        String[] ignoredOnStrs = attributes.getStringArray(IGNORED_ON_ATTRIBUTES);
        for (String ignoredOn : ignoredOnStrs) {
            if (boundedContext.containsKey(ignoredOn)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, Object> getQueryParamsFromContext(EntityDefinition entityDefinition, BoundedContext boundedContext, Object rootEntity) {
        Map<String, Object> queryParams = new LinkedHashMap<>(4);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTES);
                queryParams.put(fieldAttribute, boundValue);
            }
        }
        return queryParams;
    }

    protected Object getBoundValue(BindingDefinition bindingDefinition, BoundedContext boundedContext, Object rootEntity) {
        Object boundValue;
        if (bindingDefinition.isFromContext()) {
            AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
            String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTES);
            boundValue = boundedContext.get(bindAttribute);
        } else {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            boundValue = boundEntityPropertyChain.getValue(rootEntity);
        }
        return boundValue;
    }

    protected void handleSingleEntity(BoundedContext boundedContext,
                                      Object rootEntity,
                                      EntityDefinition entityDefinition,
                                      EntityPropertyChain entityPropertyChain,
                                      Object lastEntity,
                                      Object persistentObject) {
        EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
        Object entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObject);
        if (entity != null) {
            EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
            entityProperty.setValue(lastEntity, entity);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> findByExample(BoundedContext boundedContext, Object example) {
        Assert.notNull(rootEntityDefinition, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        Object persistentObject = doSelectByExample(rootEntityDefinition.getMapper(), boundedContext, true, example);
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
        Assert.notNull(entity, "The entity cannot be null!");
        for (EntityDefinition entityDefinition : orderedEntityDefinitions) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        insertSingleEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    Object primaryKey = insertSingleEntity(boundedContext, entity, entityDefinition, targetEntity);
                    setBoundIdForBoundEntity(entityDefinition, entity, primaryKey);
                }
            }
        }
    }

    protected Object insertSingleEntity(BoundedContext boundedContext, Object rootEntity,
                                        EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            getBoundValueFromContext(boundedContext, rootEntity, entityDefinition, entity);
            EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, rootEntity, entityDefinition, entity);
            if (persistentObject != null) {
                doInsert(entityDefinition.getMapper(), boundedContext, persistentObject);
                primaryKey = copyPrimaryKeyToEntity(entity, persistentObject);
            }
        }
        return primaryKey;
    }

    protected void getBoundValueFromContext(BoundedContext boundedContext, Object rootEntity,
                                            EntityDefinition entityDefinition, Object entity) {
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTES);
                BeanUtil.setFieldValue(entity, fieldAttribute, boundValue);
            }
        }
    }

    protected Object copyPrimaryKeyToEntity(Object entity, Object persistentObject) {
        Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
        BeanUtil.setFieldValue(entity, "id", primaryKey);
        return primaryKey;
    }

    protected void setBoundIdForBoundEntity(EntityDefinition entityDefinition, Object rootEntity, Object primaryKey) {
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null && primaryKey != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, primaryKey);
        }
    }

    @Override
    public void update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        for (EntityDefinition entityDefinition : orderedEntityDefinitions) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        updateSingleEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    updateSingleEntity(boundedContext, entity, entityDefinition, targetEntity);
                }
            }
        }
    }

    protected void updateSingleEntity(BoundedContext boundedContext, Object rootEntity,
                                      EntityDefinition entityDefinition, Object entity) {
        EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
        Object persistentObject = entityAssembler.disassemble(boundedContext, rootEntity, rootEntityDefinition, entity);
        if (persistentObject != null) {
            doUpdate(entityDefinition.getMapper(), boundedContext, persistentObject);
        }
    }

    @Override
    public void delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        for (EntityDefinition entityDefinition : orderedEntityDefinitions) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        deleteSingleEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    deleteSingleEntity(boundedContext, entity, entityDefinition, targetEntity);
                }
            }
        }
    }

    protected void deleteSingleEntity(BoundedContext boundedContext, Object rootEntity,
                                      EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            doDeleteByPrimaryKey(entityDefinition.getMapper(), boundedContext, primaryKey);
        }
    }

    protected abstract Object doSelectByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

    protected abstract Object doSelectByExample(Object mapper, BoundedContext boundedContext, boolean manyToOne, Object example);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDeleteByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

}
