package com.gitee.dorive.executor.v1.impl.handler.cond;

import com.gitee.dorive.base.v1.executor.api.ConditionHandler;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.entity.cop.ConditionDelete;
import com.gitee.dorive.base.v1.executor.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.executor.entity.op.Condition;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.util.ExampleUtils;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class DefaultConditionHandler implements ConditionHandler {

    private final RepositoryContext repositoryContext;

    @Override
    public long handle(Context context, Condition condition) {
        final AtomicInteger totalCount = new AtomicInteger(0);
        if (condition instanceof ConditionUpdate conditionUpdate) {
            execute(context, condition, totalCount, (RepositoryItem repositoryItem, boolean isMatch) -> {
                OperationFactory operationFactory = repositoryItem.getOperationFactory();
                Operation operation = operationFactory.buildUpdateByExample( //
                        conditionUpdate.getEntity(), ExampleUtils.clone(condition.getExample()));
                operation.switchRoot(isMatch);
                totalCount.addAndGet(repositoryItem.execute(context, operation));
            });

        } else if (condition instanceof ConditionDelete) {
            execute(context, condition, totalCount, (RepositoryItem repositoryItem, boolean isMatch) -> {
                OperationFactory operationFactory = repositoryItem.getOperationFactory();
                Operation operation = operationFactory.buildDeleteByExample( //
                        ExampleUtils.clone(condition.getExample()));
                operation.switchRoot(isMatch);
                totalCount.addAndGet(repositoryItem.execute(context, operation));
            });
        }
        return totalCount.get();
    }

    private void execute(Context context, Condition condition, AtomicInteger totalCount, Executor executor) {
        for (RepositoryItem repositoryItem : repositoryContext.getOrderedRepositories()) {
            if (repositoryItem.isRoot()) {
                if (condition.isNotIgnoreRoot()) {
                    boolean isMatch = repositoryContext.matches(context, repositoryItem);
                    if (isMatch || condition.isIncludeRoot()) {
                        totalCount.addAndGet(repositoryItem.execute(context, condition));
                    }
                }
            } else {
                boolean isMatch = repositoryContext.matches(context, repositoryItem);
                if (isMatch || repositoryItem.isAggregated()) {
                    executor.execute(repositoryItem, isMatch);
                }
            }
        }
    }

    private interface Executor {
        void execute(RepositoryItem repositoryItem, boolean isMatch);
    }

}
