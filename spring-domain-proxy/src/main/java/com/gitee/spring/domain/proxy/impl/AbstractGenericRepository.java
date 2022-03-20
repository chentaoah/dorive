package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.api.IRepository;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

public abstract class AbstractGenericRepository<E, PK> extends AbstractEntityDefinitionResolver implements IRepository<E, PK> {

    @Override
    protected Class<?> getTargetClass() {
        return AbstractGenericRepository.class;
    }

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
        if (rootEntity == null) {
            return null;
        }
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityProperty();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null) {
                AnnotationAttributes attributes = entityDefinition.getAttributes();
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
                        entityProperty.setValue(rootEntity, entity);
                    }
                }
            }
        }
        return rootEntity;
    }

    @SuppressWarnings("unchecked")
    protected E newInstance(BoundedContext boundedContext, PK primaryKey) {
        return (E) ReflectUtils.newInstance(constructor, null);
    }

    @Override
    public E findByPrimaryKey(PK primaryKey) {
        return findByPrimaryKey(new BoundedContext(), primaryKey);
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

    protected void handleEntity(E entity, Consumer consumer) {
        Assert.notNull(entity, "The entity cannot be null!");
        BoundedContext boundedContext = new BoundedContext();
        if (rootEntityDefinition != null) {
            EntityAssembler entityAssembler = rootEntityDefinition.getEntityAssembler();
            Object persistentObject = entityAssembler.disassemble(boundedContext, entity, rootEntityDefinition, entity);
            if (persistentObject != null) {
                consumer.accept(rootEntityDefinition.getMapper(), persistentObject);
            }
        }
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
            Object accessEntity = entityProperty.getValue(entity);
            if (accessEntity != null) {
                EntityAssembler entityAssembler = entityDefinition.getEntityAssembler();
                Object persistentObject = entityAssembler.disassemble(boundedContext, entity, entityDefinition, accessEntity);
                if (persistentObject != null) {
                    consumer.accept(entityDefinition.getMapper(), persistentObject);
                }
            }
        }
    }

    protected abstract Object doSelectByPrimaryKey(Object mapper, BoundedContext boundedContext, PK primaryKey);

    protected abstract Object doSelectByContext(Object mapper, BoundedContext boundedContext, boolean manyToOne);

    protected abstract Object doSelectByQueryField(Object mapper, BoundedContext boundedContext, boolean manyToOne, String queryField, Object queryValue);

    protected abstract void doInsert(Object mapper, Object persistentObject);

    protected abstract void doUpdate(Object mapper, Object persistentObject);

    protected abstract void doDelete(Object mapper, Object persistentObject);

    public interface Consumer {
        void accept(Object mapper, Object persistentObject);
    }

}
