package com.gitee.dorive.core.impl.observe;

import com.gitee.dorive.core.api.Observed;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.impl.OperationFactory;
import com.gitee.dorive.core.impl.OperationTypeResolver;
import com.gitee.dorive.core.impl.selector.NameSelector;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public class DeleteList implements Observed {

    private List<?> listToDelete;
    private String[] namesToAdd;

    public DeleteList(Object object) {
        if (object instanceof Collection) {
            this.listToDelete = new ArrayList<>((Collection<?>) object);
        } else {
            this.listToDelete = Collections.singletonList(object);
        }
    }

    public DeleteList(Object object, String... namesToAdd) {
        this(object);
        this.namesToAdd = namesToAdd;
    }

    @Override
    public ObservedResult accept(CommonRepository repository, BoundedContext boundedContext, Object entity) {
        if (namesToAdd != null && namesToAdd.length > 0) {
            Selector selector = boundedContext.getSelector();
            if (selector instanceof NameSelector) {
                NameSelector nameSelector = new NameSelector();
                for (String name : namesToAdd) {
                    nameSelector.resolveName(name);
                }
            }
        }

        int totalCount = 0;
        OperationFactory operationFactory = repository.getOperationFactory();
        for (Object entityToDelete : listToDelete) {
            Object primaryKey = repository.getPrimaryKey(entityToDelete);
            int operationType = OperationTypeResolver.mergeOperationType(Operation.DELETE, Operation.NONE, primaryKey);
            if (operationType == Operation.DELETE) {
                Delete delete = operationFactory.buildDelete(boundedContext, entityToDelete);
                delete.setType(delete.getType() | Operation.INCLUDE_ROOT);
                totalCount += repository.execute(boundedContext, delete);
            }
        }

        return new ObservedResult(Operation.INSERT_OR_UPDATE_OR_DELETE, totalCount);
    }

}
