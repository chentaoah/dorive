package com.gitee.spring.domain.core.mapper;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

public class NoBuiltEntityMapper extends ProxyEntityMapper {

    public NoBuiltEntityMapper(EntityMapper entityMapper) {
        super(entityMapper);
    }

    @Override
    public EntityExample newExample(EntityDefinition entityDefinition, BoundedContext boundedContext) {
        EntityExample entityExample = super.newExample(entityDefinition, boundedContext);
        return new EntityExample(entityExample.getExample()) {
            @Override
            public Object getBuiltExample() {
                return this;
            }
        };
    }

}
