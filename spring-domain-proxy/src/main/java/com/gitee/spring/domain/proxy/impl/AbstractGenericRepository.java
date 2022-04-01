package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.api.EntitySelector;
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
            if (lastEntity != null && isMatchScenes(boundedContext, rootEntity, entityDefinition)) {
                EntitySelector entitySelector = entityDefinition.getEntitySelector();
                Object entity = entitySelector.select(this, boundedContext, rootEntity, entityDefinition);
                if (entity != null) {
                    EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                    entityProperty.setValue(lastEntity, entity);
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

    protected Object getQueryParamsFromContext(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        Object queryParams = newQueryParams(boundedContext, rootEntity, entityDefinition);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(boundedContext, rootEntity, bindingDefinition);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                addToQueryParams(queryParams, fieldAttribute, boundValue);
            }
        }
        return queryParams;
    }

    protected Object newQueryParams(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        return new LinkedHashMap<>();
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

    @SuppressWarnings("unchecked")
    protected void addToQueryParams(Object queryParams, String fieldAttribute, Object boundValue) {
        Map<String, Object> queryParamsMap = (Map<String, Object>) queryParams;
        queryParamsMap.put(fieldAttribute, boundValue);
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
            return (List<E>) createRootEntities(boundedContext, persistentObjects);
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T findPageByExample(BoundedContext boundedContext, Object example, Object page) {
        Assert.notNull(rootEntityDefinition, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        Object dataPage = doSelectPageByExample(rootEntityDefinition.getMapper(), boundedContext, example, page);
        List<?> persistentObjects = getDataFromPage(dataPage);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            List<Object> rootEntities = createRootEntities(boundedContext, persistentObjects);
            return (T) newPageOfEntities(dataPage, rootEntities);
        }
        return null;
    }

    protected List<Object> createRootEntities(BoundedContext boundedContext, List<?> persistentObjects) {
        EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
        List<Object> rootEntities = new ArrayList<>();
        for (Object persistentObject : persistentObjects) {
            Object rootEntity = entityAssembler.assemble(boundedContext, null, rootEntityDefinition, persistentObject);
            handleRootEntity(boundedContext, rootEntity);
            rootEntities.add(rootEntity);
        }
        return rootEntities;
    }

    @Override
    public void insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepositoryForRootEntity(entity);
        for (EntityDefinition entityDefinition : orderedEntityDefinitions) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, entity, entityDefinition)) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        insertEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    Object primaryKey = insertEntity(boundedContext, entity, entityDefinition, targetEntity);
                    setBoundIdForBoundEntity(boundedContext, entity, entityDefinition, primaryKey);
                }
            }
        }
    }

    protected Object insertEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            getBoundValueFromContext(boundedContext, rootEntity, entityDefinition, entity);
            EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, rootEntity, entityDefinition, entity);
            if (persistentObject != null) {
                doInsert(entityDefinition.getMapper(), boundedContext, persistentObject);
                primaryKey = copyPrimaryKeyForEntity(entity, persistentObject);
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

    protected Object copyPrimaryKeyForEntity(Object entity, Object persistentObject) {
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
            if (targetEntity != null && isMatchScenes(boundedContext, entity, entityDefinition)) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        updateEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    updateEntity(boundedContext, entity, entityDefinition, targetEntity);
                }
            }
        }
    }

    protected void updateEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
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
            if (targetEntity != null && isMatchScenes(boundedContext, entity, entityDefinition)) {
                if (entityDefinition.isCollection()) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        deleteEntity(boundedContext, entity, entityDefinition, eachEntity);
                    }
                } else {
                    deleteEntity(boundedContext, entity, entityDefinition, targetEntity);
                }
            }
        }
    }

    protected void deleteEntity(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            doDeleteByPrimaryKey(entityDefinition.getMapper(), boundedContext, primaryKey);
        }
    }

    protected abstract Object doSelectByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

    protected abstract List<?> doSelectByExample(Object mapper, BoundedContext boundedContext, Object example);

    protected abstract Object doSelectPageByExample(Object mapper, BoundedContext boundedContext, Object example, Object page);

    protected abstract List<?> getDataFromPage(Object dataPage);

    protected abstract Object newPageOfEntities(Object dataPage, List<Object> entities);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDeleteByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

}
