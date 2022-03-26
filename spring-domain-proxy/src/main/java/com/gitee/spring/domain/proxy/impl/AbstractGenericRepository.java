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
                    EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                    Object entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, persistentObject);
                    if (entity != null) {
                        EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                        entityProperty.setValue(lastEntity, entity);
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
        Map<String, Object> queryParams = new LinkedHashMap<>();
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
                boolean isCollection = targetEntity instanceof Collection;
                getAssociationIdFromContext(entityDefinition, boundedContext, entity, targetEntity, isCollection);
                EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                Object persistentObject = entityAssembler.disassemble(boundedContext, entity, entityDefinition, targetEntity);
                if (persistentObject != null) {
                    doInsert(entityDefinition.getMapper(), boundedContext, persistentObject);
                    if (!isCollection) {
                        copyPrimaryKeyForEntity(targetEntity, persistentObject);
                        setAssociationIdForAnotherEntity(entityDefinition, entity, targetEntity);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void getAssociationIdFromContext(EntityDefinition entityDefinition, BoundedContext boundedContext,
                                               Object rootEntity, Object entity, boolean isCollection) {
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue instanceof Number) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTES);
                if (isCollection) {
                    Collection<Object> collection = (Collection<Object>) entity;
                    collection.forEach(eachEntity -> BeanUtil.setFieldValue(eachEntity, fieldAttribute, boundValue));
                } else {
                    BeanUtil.setFieldValue(entity, fieldAttribute, boundValue);
                }
            }
        }
    }

    protected void copyPrimaryKeyForEntity(Object entity, Object persistentObject) {
        Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
        BeanUtil.setFieldValue(entity, "id", primaryKey);
    }

    protected void setAssociationIdForAnotherEntity(EntityDefinition entityDefinition, Object rootEntity, Object entity) {
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, BeanUtil.getFieldValue(entity, "id"));
        }
    }

    @Override
    public void update(BoundedContext boundedContext, E entity) {
        handleEntities(boundedContext, entity, this::doUpdate);
    }

    @Override
    public void delete(BoundedContext boundedContext, E entity) {
        handleEntities(boundedContext, entity, this::doDelete);
    }

    protected void handleEntities(BoundedContext boundedContext, E entity, Consumer consumer) {
        Assert.notNull(entity, "The entity cannot be null!");
        if (rootEntityDefinition != null) {
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, entity, rootEntityDefinition, entity);
            if (persistentObject != null) {
                consumer.accept(rootEntityDefinition.getMapper(), boundedContext, persistentObject);
            }
        }
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                Object persistentObject = entityAssembler.disassemble(boundedContext, entity, entityDefinition, targetEntity);
                if (persistentObject != null) {
                    consumer.accept(entityDefinition.getMapper(), boundedContext, persistentObject);
                }
            }
        }
    }

    protected abstract Object doSelectByPrimaryKey(Object mapper, BoundedContext boundedContext, PK primaryKey);

    protected abstract Object doSelectByExample(Object mapper, BoundedContext boundedContext, boolean manyToOne, Object example);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDelete(Object mapper, BoundedContext boundedContext, Object persistentObject);

    public interface Consumer {
        void accept(Object mapper, BoundedContext boundedContext, Object persistentObject);
    }

}
