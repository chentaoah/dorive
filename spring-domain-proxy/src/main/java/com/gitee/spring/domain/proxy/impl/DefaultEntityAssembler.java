package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

import java.util.ArrayList;
import java.util.List;

public class DefaultEntityAssembler implements EntityAssembler {

    @Override
    public Object assemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object persistentObject) {
        if (persistentObject instanceof List) {
            List<?> list = (List<?>) persistentObject;
            if (!list.isEmpty()) {
                List<Object> entities = new ArrayList<>();
                for (Object item : list) {
                    Object entity = BeanUtil.copyProperties(item, entityDefinition.getEntityClass());
                    entities.add(entity);
                }
                return entities;
            }
        } else {
            return BeanUtil.copyProperties(persistentObject, entityDefinition.getEntityClass());
        }
        return null;
    }

    @Override
    public Object disassemble(BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition, Object entity) {
        if (entity instanceof List) {
            List<?> list = (List<?>) entity;
            if (!list.isEmpty()) {
                List<Object> persistentObjects = new ArrayList<>();
                for (Object item : list) {
                    Object persistentObject = BeanUtil.copyProperties(item, entityDefinition.getPojoClass());
                    persistentObjects.add(persistentObject);
                }
                return persistentObjects;
            }
        } else {
            return BeanUtil.copyProperties(entity, entityDefinition.getPojoClass());
        }
        return null;
    }

}
