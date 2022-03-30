package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.api.RepositoryAware;
import com.gitee.spring.domain.proxy.entity.*;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

public abstract class AbstractGenericRepository<E, PK> extends AbstractRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity = null;
        if (rootEntityDefinition != null) {
            Object persistentObject = doSelectByPrimaryKey(rootEntityDefinition.getMapper(), boundedContext, primaryKey);
            if (persistentObject != null) {
                EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
                rootEntity = entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
            }
        } else {
            rootEntity = ReflectUtils.newInstance(constructor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
        }
        return (E) rootEntity;
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        bindRepositoryForRootEntity(rootEntity);
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityProperty();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null) {
                if (isMatchScenes(boundedContext, rootEntity, entityDefinition)) {
                    Map<String, Object> queryParams = getQueryParamsFromContext(boundedContext, rootEntity, entityDefinition);
                    List<?> persistentObjects = doSelectByExample(entityDefinition.getMapper(), boundedContext, queryParams);
                    if (persistentObjects != null && !persistentObjects.isEmpty()) {
                        Object entity = assembleEntity(boundedContext, rootEntity, entityDefinition, persistentObjects);
                        if (entity != null) {
                            EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                            entityProperty.setValue(lastEntity, entity);
                        }
                    }
                }
            }
        }
    }

    protected void bindRepositoryForRootEntity(Object rootEntity) {
        if (rootEntity instanceof RepositoryAware) {
            ((RepositoryAware) rootEntity).setRepository(this);
        }
    }

    protected boolean isMatchScenes(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        AnnotationAttributes attributes = entityDefinition.getAttributes();
        String[] sceneAttribute = attributes.getStringArray(SCENE_ATTRIBUTE);
        if (sceneAttribute.length == 0) {
            return true;
        }
        for (String scene : sceneAttribute) {
            if (boundedContext.containsKey(scene)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, Object> getQueryParamsFromContext(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(boundedContext, rootEntity, bindingDefinition);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                fieldAttribute = convertFieldName(entityDefinition, bindingDefinition, fieldAttribute);
                queryParams.put(fieldAttribute, boundValue);
            }
        }
        return queryParams;
    }

    protected Object getBoundValue(BoundedContext boundedContext, Object rootEntity, BindingDefinition bindingDefinition) {
        Object boundValue;
        if (bindingDefinition.isFromContext()) {
            AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
            String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);
            boundValue = boundedContext.get(bindAttribute);
        } else {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            boundValue = boundEntityPropertyChain.getValue(rootEntity);
        }
        return boundValue;
    }

    protected String convertFieldName(EntityDefinition entityDefinition, BindingDefinition bindingDefinition, String fieldAttribute) {
        return fieldAttribute;
    }

    protected Object assembleEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, List<?> persistentObjects) {
        AnnotationAttributes attributes = entityDefinition.getAttributes();
        EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
        Object entity;
        if (attributes.getBoolean(MANY_TO_ONE_ATTRIBUTE)) {
            entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObjects);
        } else {
            if (entityDefinition.isCollection()) {
                List<Object> entities = new ArrayList<>();
                for (Object persistentObject : persistentObjects) {
                    Object eachEntity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObject);
                    entities.add(eachEntity);
                }
                entity = entities;
            } else {
                entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObjects.get(0));
            }
        }
        return entity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> findByExample(BoundedContext boundedContext, Object example) {
        Assert.notNull(rootEntityDefinition, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        List<?> persistentObjects = doSelectByExample(rootEntityDefinition.getMapper(), boundedContext, example);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            List<Object> rootEntities = new ArrayList<>();
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            for (Object persistentObject : persistentObjects) {
                Object rootEntity = entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
                handleRootEntity(boundedContext, rootEntity);
                rootEntities.add(rootEntity);
            }
            return (List<E>) rootEntities;
        }
        return Collections.emptyList();
    }

    @Override
    public void insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepositoryForRootEntity(entity);
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
                    setBoundIdForBoundEntity(boundedContext, entity, entityDefinition, primaryKey);
                }
            }
        }
    }

    protected Object insertSingleEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
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

    protected void getBoundValueFromContext(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            if (!bindingDefinition.isBindId()) {
                Object boundValue = getBoundValue(boundedContext, rootEntity, bindingDefinition);
                if (boundValue != null) {
                    AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                    String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                    BeanUtil.setFieldValue(entity, fieldAttribute, boundValue);
                }
            }
        }
    }

    protected Object copyPrimaryKeyToEntity(Object entity, Object persistentObject) {
        Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
        BeanUtil.setFieldValue(entity, "id", primaryKey);
        return primaryKey;
    }

    protected void setBoundIdForBoundEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object primaryKey) {
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null && primaryKey != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, primaryKey);
        }
    }

    @Override
    public void update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepositoryForRootEntity(entity);
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

    protected void updateSingleEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, rootEntity, entityDefinition, entity);
            if (persistentObject != null) {
                doUpdate(entityDefinition.getMapper(), boundedContext, persistentObject);
            }
        }
    }

    @Override
    public void delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepositoryForRootEntity(entity);
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

    protected void deleteSingleEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            doDeleteByPrimaryKey(entityDefinition.getMapper(), boundedContext, primaryKey);
        }
    }

    protected abstract Object doSelectByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

    protected abstract List<?> doSelectByExample(Object mapper, BoundedContext boundedContext, Object example);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDeleteByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

}
