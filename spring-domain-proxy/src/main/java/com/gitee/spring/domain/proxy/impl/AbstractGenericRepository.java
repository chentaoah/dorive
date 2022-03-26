package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.entity.*;
import com.gitee.spring.domain.proxy.utils.CollectionUtils;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            return rootEntity;
        }
        return null;
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
                boolean isIgnore = false;
                for (String ignoredOn : ignoredOnStrs) {
                    if (boundedContext.containsKey(ignoredOn)) {
                        isIgnore = true;
                        break;
                    }
                }
                if (isIgnore) {
                    continue;
                }
                Map<String, Object> queryParams = new LinkedHashMap<>();
                for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                    String field = bindingDefinition.getAttributes().getString(FIELD_ATTRIBUTES);
                    if (bindingDefinition.isFromContext()) {
                        String bind = bindingDefinition.getAttributes().getString(BIND_ATTRIBUTES);
                        Object bindValue = boundedContext.get(bind);
                        queryParams.put(field, bindValue);
                    } else {
                        EntityPropertyChain bindEntityPropertyChain = bindingDefinition.getBindEntityPropertyChain();
                        Object bindValue = bindEntityPropertyChain.getValue(rootEntity);
                        queryParams.put(field, bindValue);
                    }
                }
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
        if (rootEntityDefinition != null) {
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, entity, rootEntityDefinition, entity);
            if (persistentObject != null) {
                doInsert(rootEntityDefinition.getMapper(), boundedContext, persistentObject);
                copyPrimaryKeyForEntity(entity, persistentObject);
            }
        }
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain.getValue(entity);
            if (targetEntity != null) {
                bindRelationIdForEntity(entityDefinition, boundedContext, entity, targetEntity);
                EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                Object persistentObject = entityAssembler.disassemble(boundedContext, entity, entityDefinition, targetEntity);
                if (persistentObject != null) {
                    doInsert(entityDefinition.getMapper(), boundedContext, persistentObject);
                    copyPrimaryKeyForEntity(entity, persistentObject);
                }
            }
        }
    }

    protected void copyPrimaryKeyForEntity(Object entity, Object persistentObject) {
        if (!(entity instanceof List)) {
            Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
            BeanUtil.setFieldValue(entity, "id", primaryKey);
        }
    }

    protected void bindRelationIdForEntity(EntityDefinition entityDefinition, BoundedContext boundedContext, Object rootEntity, Object entity) {
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            String field = bindingDefinition.getAttributes().getString(FIELD_ATTRIBUTES);
            Object bindValue;
            if (bindingDefinition.isFromContext()) {
                String bind = bindingDefinition.getAttributes().getString(BIND_ATTRIBUTES);
                bindValue = boundedContext.get(bind);
            } else {
                EntityPropertyChain bindEntityPropertyChain = bindingDefinition.getBindEntityPropertyChain();
                bindValue = bindEntityPropertyChain.getValue(rootEntity);
            }
            if (bindValue != null) {
                CollectionUtils.forEach(entity, eachEntity -> BeanUtil.setFieldValue(eachEntity, field, bindValue));
            }
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
