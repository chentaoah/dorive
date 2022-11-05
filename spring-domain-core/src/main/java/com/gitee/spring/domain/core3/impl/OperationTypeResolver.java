package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
import org.apache.commons.lang3.StringUtils;

public class OperationTypeResolver {

    public int resolveOperationType(BoundedContext boundedContext, ConfiguredRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();

        String forceIgnoreKey = entityDefinition.getForceIgnoreKey();
        if (StringUtils.isNotBlank(forceIgnoreKey) && boundedContext.containsKey(forceIgnoreKey)) {
            return Operation.FORCE_IGNORE;
        }

        String forceInsertKey = entityDefinition.getForceInsertKey();
        if (StringUtils.isNotBlank(forceInsertKey) && boundedContext.containsKey(forceInsertKey)) {
            return Operation.FORCE_INSERT;
        }

        return Operation.NONE;
    }

    public int mergeOperationType(int expectedOperationType, int contextOperationType, Object entity) {
        if (contextOperationType == Operation.FORCE_IGNORE) {
            return Operation.FORCE_IGNORE;

        } else if (contextOperationType == Operation.FORCE_INSERT) {
            return expectedOperationType & Operation.INSERT;

        } else if (expectedOperationType == Operation.INSERT_OR_UPDATE) {
            return Operation.INSERT_OR_UPDATE;

        } else {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            contextOperationType = primaryKey == null ? Operation.INSERT : Operation.UPDATE_OR_DELETE;
            return expectedOperationType & contextOperationType;
        }
    }

}
