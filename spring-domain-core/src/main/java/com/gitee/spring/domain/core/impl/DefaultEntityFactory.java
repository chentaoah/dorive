package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.api.EntityFactory;
import com.gitee.spring.domain.core.entity.definition.ElementDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private ElementDefinition elementDefinition;
    private Class<?> pojoClass;

    @Override
    public Object reconstitute(BoundedContext boundedContext, Object persistentObject) {
        if (persistentObject instanceof Map) {
            return BeanUtil.mapToBean((Map<?, ?>) persistentObject, elementDefinition.getGenericEntityClass(), true, CopyOptions.create().ignoreNullValue());
        } else {
            return BeanUtil.copyProperties(persistentObject, elementDefinition.getGenericEntityClass());
        }
    }

    @Override
    public Object deconstruct(BoundedContext boundedContext, Object entity) {
        return BeanUtil.copyProperties(entity, pojoClass);
    }

}
