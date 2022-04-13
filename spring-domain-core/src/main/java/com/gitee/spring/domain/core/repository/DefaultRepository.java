package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DefaultRepository extends AbstractRepository<Object, Object> {

    protected EntityPropertyChain entityPropertyChain;
    protected EntityDefinition entityDefinition;
    protected EntityMapper entityMapper;
    protected EntityAssembler entityAssembler;

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        Object persistentObject = entityMapper.selectByPrimaryKey(entityDefinition.getMapper(), boundedContext, primaryKey);
        if (persistentObject != null) {
            return entityAssembler.assemble(entityDefinition, boundedContext, persistentObject);
        }
        return null;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        List<?> persistentObjects = entityMapper.selectByExample(entityDefinition.getMapper(), boundedContext, example);
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
        Object dataPage = entityMapper.selectPageByExample(entityDefinition.getMapper(), boundedContext, example, page);
        List<?> persistentObjects = entityMapper.getDataFromPage(dataPage);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            List<Object> entities = newEntities(boundedContext, persistentObjects);
            return (T) entityMapper.newPageOfEntities(dataPage, entities);
        }
        return (T) dataPage;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        int count = 0;
        if (entity instanceof Collection) {
            for (Object eachEntity : (Collection<?>) entity) {
                count += doInsert(boundedContext, eachEntity);
            }
        } else {
            count += doInsert(boundedContext, entity);
        }
        return count;
    }

    protected int doInsert(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            Object persistentObject = entityAssembler.disassemble(entityDefinition, boundedContext, entity);
            if (persistentObject != null) {
                int count = entityMapper.insert(entityDefinition.getMapper(), boundedContext, persistentObject);
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
        int count = 0;
        if (entity instanceof Collection) {
            for (Object eachEntity : (Collection<?>) entity) {
                count += doUpdate(boundedContext, eachEntity);
            }
        } else {
            count += doUpdate(boundedContext, entity);
        }
        return count;
    }

    protected int doUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            Object persistentObject = entityAssembler.disassemble(entityDefinition, boundedContext, entity);
            if (persistentObject != null) {
                return entityMapper.update(entityDefinition.getMapper(), boundedContext, persistentObject);
            }
        }
        return 0;
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        Assert.isTrue(!(entity instanceof Collection), "The entity cannot be a collection!");
        Object persistentObject = entityAssembler.disassemble(entityDefinition, new BoundedContext(), entity);
        if (persistentObject != null) {
            return entityMapper.updateByExample(entityDefinition.getMapper(), persistentObject, example);
        }
        return 0;
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        int count = 0;
        if (entity instanceof Collection) {
            for (Object eachEntity : (Collection<?>) entity) {
                count += doDelete(boundedContext, eachEntity);
            }
        } else {
            count += doDelete(boundedContext, entity);
        }
        return count;
    }

    protected int doDelete(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            return entityMapper.deleteByPrimaryKey(entityDefinition.getMapper(), boundedContext, primaryKey);
        }
        return 0;
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        return entityMapper.deleteByPrimaryKey(entityDefinition.getMapper(), new BoundedContext(), primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        return entityMapper.deleteByExample(entityDefinition.getMapper(), example);
    }

}
