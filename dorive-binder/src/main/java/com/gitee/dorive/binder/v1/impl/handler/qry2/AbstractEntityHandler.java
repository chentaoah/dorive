package com.gitee.dorive.binder.v1.impl.handler.qry2;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public abstract class AbstractEntityHandler implements EntityHandler {

    private final RepositoryItem repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        Example example = newExample(context, entities);
        appendFilterCriteria(context, example);
        if (example.isEmpty()) {
            return 0L;
        }
        OperationFactory operationFactory = repository.getOperationFactory();
        EntityJoiner entityJoiner = repository.getProperty(EntityJoiner.class);
        Query query = operationFactory.buildQueryByExample(example);
        query.includeRoot();
        Result<Object> result = repository.executeQuery(context, query);
        entityJoiner.join(context, entities, result.getRecords());
        return result.getCount();
    }

    protected void appendFilterCriteria(Context context, Example example) {
        if (example != null && !example.isEmpty()) {
            BinderResolver binderResolver = (BinderResolver) repository.getBinderExecutor();
            List<Binder> weakBinders = binderResolver.getWeakBinders();
            for (Binder weakBinder : weakBinders) {
                Object boundValue = weakBinder.input(context, null);
                if (boundValue != null) {
                    String fieldName = weakBinder.getFieldName();
                    example.eq(fieldName, boundValue);
                }
            }
            binderResolver.appendFilterValue(context, example);
        }
    }

    protected abstract Example newExample(Context context, List<Object> entities);

}
