package com.gitee.spring.domain.proxy.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.*;
import com.gitee.spring.domain.proxy.entity.*;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity;
        if (rootRepository != null) {
            rootEntity = rootRepository.selectByPrimaryKey(boundedContext, primaryKey);
        } else {
            rootEntity = ReflectUtils.newInstance(constructor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
        }
        return (E) rootEntity;
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        bindRepository(rootEntity);
        for (DefaultRepository defaultRepository : defaultRepositories) {
            EntityPropertyChain entityPropertyChain = defaultRepository.getEntityPropertyChain();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityProperty();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null && isMatchScenes(boundedContext, entityDefinition)) {
                Object queryParams = getQueryParamsFromContext(boundedContext, rootEntity, defaultRepository);
                List<?> entities = defaultRepository.selectByExample(boundedContext, queryParams);
                Object entity = convertManyToOneEntity(entityDefinition, entities);
                if (entity != null) {
                    EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                    entityProperty.setValue(lastEntity, entity);
                }
            }
        }
    }

    protected void bindRepository(Object entity) {
        if (entity instanceof RepositoryAware) {
            ((RepositoryAware) entity).setRepository(this);
        }
    }

    protected boolean isMatchScenes(BoundedContext boundedContext, EntityDefinition entityDefinition) {
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

    protected Object getQueryParamsFromContext(BoundedContext boundedContext, Object rootEntity, DefaultRepository defaultRepository) {
        EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
        EntityMapper entityMapper = defaultRepository.getEntityMapper();
        Object queryParams = entityMapper.newQueryParams(boundedContext, entityDefinition);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(boundedContext, rootEntity, bindingDefinition);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                entityMapper.addToQueryParams(queryParams, fieldAttribute, boundValue);
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

    protected Object convertManyToOneEntity(EntityDefinition entityDefinition, List<?> entities) {
        if (entityDefinition.isCollection()) {
            return entities;
        } else if (!entities.isEmpty()) {
            return entities.get(0);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Object example) {
        Assert.notNull(rootRepository, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        List<?> entities = rootRepository.selectByExample(boundedContext, example);
        entities.forEach(entity -> handleRootEntity(boundedContext, entity));
        return (List<E>) entities;
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        Assert.notNull(rootRepository, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        T dataPage = rootRepository.selectPageByExample(boundedContext, example, page);
        EntityMapper entityMapper = rootRepository.getEntityMapper();
        List<?> entities = entityMapper.getDataFromPage(dataPage);
        entities.forEach(entity -> handleRootEntity(boundedContext, entity));
        return dataPage;
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepository(entity);
        int count = 0;
        for (DefaultRepository defaultRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = defaultRepository.getEntityPropertyChain();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, entityDefinition)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        getBoundValueFromContext(boundedContext, entity, entityDefinition, eachEntity);
                        count += defaultRepository.doInsert(boundedContext, eachEntity);
                    }
                } else {
                    getBoundValueFromContext(boundedContext, entity, entityDefinition, targetEntity);
                    count += defaultRepository.doInsert(boundedContext, targetEntity);
                    Object primaryKey = BeanUtil.getFieldValue(targetEntity, "id");
                    setBoundIdForBoundEntity(entity, entityDefinition, primaryKey);
                }
            }
        }
        return count;
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

    protected void setBoundIdForBoundEntity(Object rootEntity, EntityDefinition entityDefinition, Object primaryKey) {
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null && primaryKey != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, primaryKey);
        }
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepository(entity);
        int count = 0;
        for (DefaultRepository defaultRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = defaultRepository.getEntityPropertyChain();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, entityDefinition)) {
                count += defaultRepository.update(boundedContext, targetEntity);
            }
        }
        return count;
    }

    @Override
    public int updateByExample(E entity, Object example) {
        return rootRepository.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepository(entity);
        int count = 0;
        for (DefaultRepository defaultRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = defaultRepository.getEntityPropertyChain();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, entityDefinition)) {
                count += defaultRepository.delete(boundedContext, targetEntity);
            }
        }
        return count;
    }

    @Override
    public int deleteByPrimaryKey(PK primaryKey) {
        BoundedContext boundedContext = new BoundedContext();
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(new BoundedContext(), entity);
    }

    @Override
    public int deleteByExample(Object example) {
        return rootRepository.deleteByExample(example);
    }

}
