package com.gitee.dorive.core.impl.operable;

import com.gitee.dorive.core.api.Operable;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.impl.OperationTypeResolver;
import com.gitee.dorive.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DeleteList implements Operable {

    private final List<?> listToDelete;

    public DeleteList(Object entity) {
        this.listToDelete = Collections.singletonList(entity);
    }

    public DeleteList(Collection<?> collection) {
        this.listToDelete = new ArrayList<>(collection);
    }

    @Override
    public OperationResult accept(ConfiguredRepository repository, BoundedContext boundedContext, Object entity) {
        int totalCount = 0;
        for (Object entityToDelete : listToDelete) {
            Object primaryKey = repository.getPrimaryKey(entityToDelete);
            int operationType = OperationTypeResolver.mergeOperationType(Operation.DELETE, Operation.NONE, primaryKey);
            if (operationType == Operation.DELETE) {
                totalCount += repository.delete(boundedContext, entityToDelete);
            }
        }
        return new OperationResult(Operation.INSERT_OR_UPDATE_OR_DELETE, totalCount);
    }
    
}
