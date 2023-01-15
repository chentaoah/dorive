package com.gitee.dorive.core.impl.operable;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.api.Operable;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.impl.OperationBuilder;
import com.gitee.dorive.core.impl.OperationTypeResolver;
import com.gitee.dorive.core.repository.ConfiguredRepository;

import java.util.*;

public class DeleteList implements Operable {

    private final String[] scenesToAdd;
    private final List<?> listToDelete;

    public DeleteList(Object entity, String... scenesToAdd) {
        this.scenesToAdd = scenesToAdd;
        this.listToDelete = Collections.singletonList(entity);
    }

    public DeleteList(Collection<?> collection, String... scenesToAdd) {
        this.scenesToAdd = scenesToAdd;
        this.listToDelete = new ArrayList<>(collection);
    }

    @Override
    public OperationResult accept(ConfiguredRepository repository, BoundedContext boundedContext, Object entity) {
        if (scenesToAdd != null && scenesToAdd.length > 0) {
            Set<String> newScenes = CollUtil.set(false, scenesToAdd);
            newScenes.addAll(boundedContext.getScenes());
            boundedContext.setScenes(newScenes);
        }

        int totalCount = 0;
        OperationBuilder operationBuilder = repository.getOperationBuilder();
        for (Object entityToDelete : listToDelete) {
            Object primaryKey = repository.getPrimaryKey(entityToDelete);
            int operationType = OperationTypeResolver.mergeOperationType(Operation.DELETE, Operation.NONE, primaryKey);
            if (operationType == Operation.DELETE) {
                Delete delete = operationBuilder.buildDelete(boundedContext, entityToDelete);
                delete.setType(delete.getType() | Operation.INCLUDE_ROOT);
                totalCount += repository.execute(boundedContext, delete);
            }
        }

        return new OperationResult(Operation.INSERT_OR_UPDATE_OR_DELETE, totalCount);
    }

}
