package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.constant.EntityState;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

public class EntityStateResolver {

    public int resolveContextEntityState(BoundedContext boundedContext, ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        String idAttribute = entityDefinition.getIdAttribute();
        if (idAttribute != null) {
            Object boundValue = boundedContext.get(idAttribute);
            if ("#forceIgnore".equals(boundValue)) {
                return EntityState.FORCE_IGNORE;

            } else if ("#forceInsert".equals(boundValue)) {
                return EntityState.FORCE_INSERT;
            }
        }
        return EntityState.NONE;
    }

    public int resolveEntityState(int expectedEntityState, int contextEntityState, Object entity) {
        if (contextEntityState == EntityState.FORCE_IGNORE) {
            return EntityState.FORCE_IGNORE;

        } else if (contextEntityState == EntityState.FORCE_INSERT) {
            return expectedEntityState & contextEntityState;

        } else if (expectedEntityState == EntityState.INSERT_OR_UPDATE) {
            return EntityState.INSERT_OR_UPDATE;

        } else {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            contextEntityState = primaryKey == null ? EntityState.INSERT : EntityState.UPDATE_SELECTIVE_OR_UPDATE_OR_DELETE;
            return expectedEntityState & contextEntityState;
        }
    }

}
