package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAccessor;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.IRepository;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;

public abstract class AbstractGenericRepository<E, PK> extends AbstractEntityDefinitionResolver implements IRepository<E, PK> {

    @Override
    protected Class<?> getTargetClass() {
        return AbstractGenericRepository.class;
    }

    @SuppressWarnings("unchecked")
    protected E newInstance(BoundedContext boundedContext, PK primaryKey) {
        return (E) ReflectUtils.newInstance(constructor, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        E rootEntity;
        if (rootEntityDefinition != null) {
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            rootEntity = (E) entityAssembler.assemble(boundedContext, null, rootEntityDefinition, primaryKey);
        } else {
            rootEntity = newInstance(boundedContext, primaryKey);
        }
        if (rootEntity != null) {
            String ignorePath = null;
            for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
                String accessPath = entityDefinition.getAccessPath();
                if (ignorePath == null || !accessPath.startsWith(ignorePath)) {
                    EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                    Object entity = entityAssembler.assemble(boundedContext, rootEntity, entityDefinition, primaryKey);
                    if (entity != null) {
                        EntityAccessor entityAccessor = entityDefinition.getEntityAccessor();
                        entityAccessor.setValue(rootEntity, entity);
                    } else {
                        ignorePath = accessPath + "/";
                    }
                }
            }
        }
        return rootEntity;
    }

    @Override
    public E findByPrimaryKey(PK primaryKey) {
        return findByPrimaryKey(new BoundedContext(), primaryKey);
    }

    protected void handleEntity(E entity, Consumer consumer) {
        Assert.notNull(entity, "The entity cannot be null!");
        BoundedContext boundedContext = new BoundedContext();
        if (rootEntityDefinition != null) {
            doHandleEntity(boundedContext, entity, rootEntityDefinition, consumer);
        }
        String ignorePath = null;
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            String accessPath = entityDefinition.getAccessPath();
            if (ignorePath == null || !accessPath.startsWith(ignorePath)) {
                if (!doHandleEntity(boundedContext, entity, entityDefinition, consumer)) {
                    ignorePath = accessPath + "/";
                }
            }
        }
    }

    protected boolean doHandleEntity(BoundedContext boundedContext, E rootEntity, EntityDefinition entityDefinition, Consumer consumer) {
        EntityAccessor entityAccessor = entityDefinition.getEntityAccessor();
        Object entity = entityAccessor.getValue(rootEntity);
        if (entity == null) {
            return false;
        }
        EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
        Object persistentObject = entityAssembler.disassemble(boundedContext, rootEntity, entityDefinition, entity);
        if (persistentObject != null) {
            consumer.accept(entityDefinition.getMapper(), persistentObject);
        }
        return true;
    }

    @Override
    public void insert(E entity) {
        handleEntity(entity, this::doInsert);
    }

    @Override
    public void update(E entity) {
        handleEntity(entity, this::doUpdate);
    }

    @Override
    public void delete(E entity) {
        handleEntity(entity, this::doDelete);
    }

    protected abstract void doInsert(Object mapper, Object persistentObject);

    protected abstract void doUpdate(Object mapper, Object persistentObject);

    protected abstract void doDelete(Object mapper, Object persistentObject);

    public interface Consumer {
        void accept(Object target, Object param);
    }

}
