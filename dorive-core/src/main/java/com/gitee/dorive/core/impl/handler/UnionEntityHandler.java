package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.api.constant.OperationType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class UnionEntityHandler implements EntityHandler {

    private final CommonRepository repository;

    @Override
    public int handle(Context context, List<Object> entities) {
        Example example = newExample(context, entities);
        if (example.isDirtyQuery()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQuery(example);
            query.setType(query.getType() | OperationType.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(entities, (MultiResult) result);
            }
            return (int) result.getCount();
        }
        return 0;
    }

    private Example newExample(Context context, List<Object> entities) {
        PropChain anchorPoint = repository.getAnchorPoint();
        PropChain lastPropChain = anchorPoint.getLastPropChain();
        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < entities.size(); index++) {
            Object entity = entities.get(index);
            Object lastEntity = lastPropChain == null ? entity : lastPropChain.getValue(entity);
            if (lastEntity != null) {
                Example example = newExampleByContext(context, entity);
                if (example.isDirtyQuery()) {
                    example.selectExtra((index + 1) + " as $row");
                    unionExample.addExample(example);
                }
            }
        }
        return unionExample;
    }

    public Example newExampleByContext(Context context, Object entity) {
        BinderResolver binderResolver = repository.getBinderResolver();
        Example example = new Example();
        for (PropertyBinder binder : binderResolver.getPropertyBinders()) {
            String fieldName = binder.getFieldName();
            Object boundValue = binder.getBoundValue(context, entity);
            if (boundValue instanceof Collection) {
                boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
            }
            if (boundValue != null) {
                boundValue = binder.input(context, boundValue);
                example.eq(fieldName, boundValue);
            } else {
                example.getCriteria().clear();
                break;
            }
        }
        if (example.isDirtyQuery()) {
            for (ContextBinder binder : binderResolver.getContextBinders()) {
                String fieldName = binder.getFieldName();
                Object boundValue = binder.getBoundValue(context, entity);
                if (boundValue != null) {
                    example.eq(fieldName, boundValue);
                }
            }
        }
        return example;
    }

    private void setValueForRootEntities(List<Object> rootEntities, MultiResult multiResult) {
        boolean isCollection = repository.getEntityEle().isCollection();
        PropChain anchorPoint = repository.getAnchorPoint();

        List<Map<String, Object>> resultMaps = multiResult.getResultMaps();
        List<Object> entities = multiResult.getRecords();
        int averageSize = entities.size() / rootEntities.size() + 1;
        int lastRowNum = -1;
        Collection<Object> lastCollection = null;

        for (int index = 0; index < entities.size(); index++) {
            Map<String, Object> resultMap = resultMaps.get(index);
            Object entity = entities.get(index);
            Integer rowNum = NumberUtils.intValue(resultMap.get("$row"));

            if (rowNum == lastRowNum) {
                if (isCollection && lastCollection != null) {
                    lastCollection.add(entity);
                }

            } else {
                Object rootEntity = rootEntities.get(rowNum - 1);
                if (isCollection) {
                    Collection<Object> collection = new ArrayList<>(averageSize);
                    anchorPoint.setValue(rootEntity, collection);
                    collection.add(entity);
                    lastCollection = collection;
                } else {
                    anchorPoint.setValue(rootEntity, entity);
                }
            }

            lastRowNum = rowNum;
        }
    }

}
