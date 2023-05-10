package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.api.constant.OperationType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiInBuilder;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.AbstractBinder;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MultiEntityHandler implements EntityHandler {

    private final CommonRepository repository;

    @Override
    public int handle(Context context, List<Object> entities) {
        Map<String, Object> entityIndex = new LinkedHashMap<>(entities.size() * 4 / 3 + 1);
        Example example = newExample(context, entities, entityIndex);
        if (example.isDirtyQuery()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQuery(example);
            query.setType(query.getType() | OperationType.INCLUDE_ROOT);
            Result<Object> result = repository.executeQuery(context, query);
            if (result instanceof MultiResult) {
                setValueForRootEntities(context, entities, entityIndex, (MultiResult) result);
            }
            return (int) result.getCount();
        }
        return 0;
    }

    private Example newExample(Context context, List<Object> entities, Map<String, Object> entityIndex) {
        BinderResolver binderResolver = repository.getBinderResolver();
        Map<String, List<PropertyBinder>> mergedBindersMap = binderResolver.getMergedBindersMap();
        List<PropertyBinder> binders = mergedBindersMap.get("/");

        Example example = new Example();
        if (binders.size() == 1) {
            PropertyBinder binder = binders.get(0);
            String fieldName = binder.getFieldName();
            List<Object> boundValues = collectBoundValues(context, entities, entityIndex, binder);
            if (!boundValues.isEmpty()) {
                if (boundValues.size() == 1) {
                    example.eq(fieldName, boundValues.get(0));
                } else {
                    example.in(fieldName, boundValues);
                }
            }

        } else {
            List<String> properties = binders.stream().map(AbstractBinder::getFieldName).collect(Collectors.toList());
            MultiInBuilder builder = new MultiInBuilder(entities.size(), properties);
            collectBoundValues(context, entities, entityIndex, binders, builder);
            if (!builder.isEmpty()) {
                example.getCriteria().add(builder.build());
            }
        }

        if (example.isDirtyQuery()) {
            for (ContextBinder binder : binderResolver.getContextBinders()) {
                String fieldName = binder.getFieldName();
                Object boundValue = binder.getBoundValue(context, null);
                if (boundValue != null) {
                    example.eq(fieldName, boundValue);
                }
            }
        }

        return example;
    }

    public List<Object> collectBoundValues(Context context, List<Object> entities, Map<String, Object> entityIndex, PropertyBinder binder) {
        List<Object> fieldValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object fieldValue = binder.getBoundValue(context, entity);
            if (fieldValue != null) {
                fieldValue = binder.input(context, fieldValue);
                fieldValues.add(fieldValue);
                entityIndex.put(String.valueOf(fieldValue), entity);
            }
        }
        return fieldValues;
    }

    private void collectBoundValues(Context context, List<Object> entities, Map<String, Object> entityIndex, List<PropertyBinder> binders,
                                    MultiInBuilder multiInBuilder) {
        for (Object entity : entities) {
            StringBuilder strBuilder = new StringBuilder();
            for (PropertyBinder binder : binders) {
                Object fieldValue = binder.getBoundValue(context, entity);
                if (fieldValue != null) {
                    fieldValue = binder.input(context, fieldValue);
                    multiInBuilder.append(fieldValue);
                    strBuilder.append(fieldValue).append(",");
                } else {
                    multiInBuilder.clear();
                    strBuilder = null;
                    break;
                }
            }
            if (strBuilder != null) {
                if (strBuilder.length() > 0) {
                    strBuilder.deleteCharAt(strBuilder.length() - 1);
                }
                entityIndex.put(strBuilder.toString(), entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setValueForRootEntities(Context context, List<Object> rootEntities, Map<String, Object> entityIndex, MultiResult multiResult) {
        boolean isCollection = repository.getEntityEle().isCollection();
        PropChain anchorPoint = repository.getAnchorPoint();

        BinderResolver binderResolver = repository.getBinderResolver();
        List<PropertyBinder> binders = binderResolver.getMergedBindersMap().get("/");

        List<Object> entities = multiResult.getRecords();
        int averageSize = entities.size() / rootEntities.size() + 1;

        for (Object entity : entities) {
            StringBuilder strBuilder = new StringBuilder();
            if (binders.size() == 1) {
                PropertyBinder binder = binders.get(0);
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    strBuilder.append(fieldValue);
                } else {
                    strBuilder = null;
                }
            } else {
                for (PropertyBinder binder : binders) {
                    Object fieldValue = binder.getFieldValue(context, entity);
                    if (fieldValue != null) {
                        strBuilder.append(fieldValue).append(",");
                    } else {
                        strBuilder = null;
                        break;
                    }
                }
                if (strBuilder != null && strBuilder.length() > 0) {
                    strBuilder.deleteCharAt(strBuilder.length() - 1);
                }
            }
            if (strBuilder != null) {
                Object rootEntity = entityIndex.get(strBuilder.toString());
                if (rootEntity != null) {
                    Object value = anchorPoint.getValue(rootEntity);
                    if (isCollection) {
                        Collection<Object> collection;
                        if (value == null) {
                            collection = new ArrayList<>(averageSize);
                            anchorPoint.setValue(rootEntity, collection);
                        } else {
                            collection = (Collection<Object>) value;
                        }
                        collection.add(entity);

                    } else {
                        if (value == null) {
                            anchorPoint.setValue(rootEntity, entity);
                        }
                    }
                }
            }
        }
    }

}
