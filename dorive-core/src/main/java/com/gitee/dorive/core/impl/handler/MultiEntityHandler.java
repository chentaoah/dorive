package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.api.constant.OperationType;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;

import java.util.List;
import java.util.Map;

public class MultiEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final OperationFactory operationFactory;

    public MultiEntityHandler(AbstractContextRepository<?, ?> repository, OperationFactory operationFactory) {
        this.repository = repository;
        this.operationFactory = operationFactory;
    }

    @Override
    public int handle(Context context, List<Object> entities) {
        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            if (selector.matches(context, repository)) {
                totalCount += executeQuery(repository, context, entities);
            }
        }
        return totalCount;
    }

    private long executeQuery(CommonRepository repository, Context context, List<Object> rootEntities) {
        Example example = newExample(repository, context, rootEntities);
        if (example.isDirtyQuery()) {
            Query query = operationFactory.buildQuery(example);
            query.setType(query.getType() | OperationType.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(repository, rootEntities, (MultiResult) result);
            }
            return result.getCount();
        }
        return 0;
    }

    private Example newExample(CommonRepository repository, Context context, List<Object> rootEntities) {
        BinderResolver binderResolver = repository.getBinderResolver();
        Map<String, List<PropertyBinder>> mergedBindersMap = binderResolver.getMergedBindersMap();

        Example example = new Example();
//        for (List<PropertyBinder> binders : mergedBindersMap.values()) {
//            if (binders.size() == 1) {
//                PropertyBinder propertyBinder = binders.get(0);
//                BindingDef bindingDef = propertyBinder.getBindingDef();
//                String field = bindingDef.getField();
//                Object boundValue = propertyBinder.getBoundValue(context, rootEntity);
//                if (boundValue instanceof Collection) {
//                    boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
//                }
//                if (boundValue != null) {
//                    boundValue = propertyBinder.input(context, boundValue);
//                    example.eq(field, boundValue);
//                } else {
//                    example.getCriteria().clear();
//                    break;
//                }
//            } else {
//
//            }
//        }
        if (example.isDirtyQuery()) {
            for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
                BindingDef bindingDef = contextBinder.getBindingDef();
                String field = bindingDef.getField();
                Object boundValue = contextBinder.getBoundValue(context, null);
                if (boundValue != null) {
                    example.eq(field, boundValue);
                }
            }
        }
        return example;
    }

    private void setValueForRootEntities(CommonRepository repository, List<Object> rootEntities, MultiResult result) {

    }

}
