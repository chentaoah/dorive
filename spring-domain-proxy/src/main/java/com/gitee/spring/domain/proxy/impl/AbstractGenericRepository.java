package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.entity.RepositoryContext;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Collections;
import java.util.List;

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
            rootEntity = newInstance(boundedContext, primaryKey);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
            return rootEntity;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected E newInstance(BoundedContext boundedContext, PK primaryKey) {
        return (E) ReflectUtils.newInstance(constructor, null);
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
                String[] ignoredOnStrs = attributes.getStringArray(IGNORED_ON_ATTRIBUTES);
                for (String ignoredOn : ignoredOnStrs) {
                    if (boundedContext.containsKey(ignoredOn)) {
                        return;
                    }
                }
                Object persistentObject = null;
                if (attributes.getBoolean(USE_CONTEXT_ATTRIBUTES)) {
                    persistentObject = doSelectByContext(entityDefinition.getMapper(), boundedContext,
                            attributes.getBoolean(MANY_TO_ONE_ATTRIBUTES));
                } else {
                    EntityPropertyChain queryValueEntityPropertyChain = entityDefinition.getQueryValueEntityPropertyChain();
                    Object queryValue = queryValueEntityPropertyChain.getValue(rootEntity);
                    if (queryValue != null) {
                        persistentObject = doSelectByQueryField(entityDefinition.getMapper(), boundedContext,
                                attributes.getBoolean(MANY_TO_ONE_ATTRIBUTES), attributes.getString(QUERY_FIELD_ATTRIBUTES), queryValue);
                    }
                }
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
        handleEntities(boundedContext, entity, this::doInsert);
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

    protected abstract Object doSelectByExample(Object mapper, BoundedContext boundedContext, Object example);

    protected abstract Object doSelectByContext(Object mapper, BoundedContext boundedContext, boolean manyToOne);

    protected abstract Object doSelectByQueryField(Object mapper, BoundedContext boundedContext, boolean manyToOne, String queryField, Object queryValue);

    protected abstract void doInsert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doUpdate(Object mapper, BoundedContext boundedContext, Object persistentObject);

    protected abstract void doDelete(Object mapper, BoundedContext boundedContext, Object persistentObject);

    public interface Consumer {
        void accept(Object mapper, BoundedContext boundedContext, Object persistentObject);
    }

}
