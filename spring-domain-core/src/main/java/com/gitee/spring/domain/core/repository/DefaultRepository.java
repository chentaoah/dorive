package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultRepository extends ConfiguredRepository {

    public DefaultRepository(EntityPropertyChain entityPropertyChain,
                             EntityDefinition entityDefinition,
                             EntityMapper entityMapper,
                             EntityAssembler entityAssembler,
                             AbstractRepository<Object, Object> repository) {
        super(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
    }

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        Object persistentObject = super.selectByPrimaryKey(boundedContext, primaryKey);
        if (persistentObject != null) {
            return entityAssembler.assemble(entityDefinition, boundedContext, persistentObject);
        }
        return null;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        List<?> persistentObjects = super.selectByExample(boundedContext, example);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            return newEntities(boundedContext, persistentObjects);
        }
        return Collections.emptyList();
    }

    protected List<Object> newEntities(BoundedContext boundedContext, List<?> persistentObjects) {
        List<Object> entities = new ArrayList<>();
        for (Object persistentObject : persistentObjects) {
            Object entity = entityAssembler.assemble(entityDefinition, boundedContext, persistentObject);
            entities.add(entity);
        }
        return entities;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        Object dataPage = super.selectPageByExample(boundedContext, example, page);
        List<?> persistentObjects = entityMapper.getDataFromPage(dataPage);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            List<Object> entities = newEntities(boundedContext, persistentObjects);
            return (T) entityMapper.newPageOfEntities(dataPage, entities);
        }
        return (T) dataPage;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            Object persistentObject = entityAssembler.disassemble(entityDefinition, boundedContext, entity);
            if (persistentObject != null) {
                int count = super.insert(boundedContext, persistentObject);
                copyPrimaryKey(entity, persistentObject);
                return count;
            }
        }
        return 0;
    }

    protected void copyPrimaryKey(Object entity, Object persistentObject) {
        Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
        BeanUtil.setFieldValue(entity, "id", primaryKey);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            Object persistentObject = entityAssembler.disassemble(entityDefinition, boundedContext, entity);
            if (persistentObject != null) {
                Object example = entityMapper.newExample(boundedContext);
                entityMapper.addToExample(example, "id", primaryKey);
                return super.updateByExample(persistentObject, example);
            }
        }
        return 0;
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        Assert.isTrue(!(entity instanceof Collection), "The entity cannot be a collection!");
        Object persistentObject = entityAssembler.disassemble(entityDefinition, new BoundedContext(), entity);
        if (persistentObject != null) {
            return super.updateByExample(persistentObject, example);
        }
        return 0;
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            return super.deleteByPrimaryKey(primaryKey);
        }
        return 0;
    }

}