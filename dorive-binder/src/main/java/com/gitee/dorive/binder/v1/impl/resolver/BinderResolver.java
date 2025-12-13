/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.binder.v1.impl.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.common.def.BindingDef;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.FieldDefinition;
import com.gitee.dorive.base.v1.common.enums.BindingType;
import com.gitee.dorive.base.v1.common.enums.JoinType;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.binder.*;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import com.gitee.dorive.binder.v1.impl.processor.SpELProcessor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;

@Data
public class BinderResolver implements BinderExecutor {

    private RepositoryContext repository;
    private List<Binder> allBinders;
    private List<Binder> strongBinders;
    private List<Binder> weakBinders;
    private List<Binder> valueRouteBinders;
    private List<Binder> valueFilterBinders;
    // 决定了关联查询具体使用哪种实现
    private Map<String, List<Binder>> mergedStrongBindersMap;
    private Map<String, List<Binder>> mergedValueRouteBindersMap;
    private Binder boundIdBinder;
    private List<String> selfFields;
    private JoinType joinType;

    public BinderResolver(RepositoryContext repository) {
        this.repository = repository;
    }

    public void resolve(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        List<BindingDef> bindingDefs = entityElement.getBindingDefs();
        Class<?> genericType = entityElement.getGenericType();
        String primaryKey = entityElement.getPrimaryKey();

        this.allBinders = new ArrayList<>(bindingDefs.size());
        this.strongBinders = new ArrayList<>(bindingDefs.size());
        this.weakBinders = new ArrayList<>(bindingDefs.size());
        this.valueRouteBinders = new ArrayList<>(bindingDefs.size());
        this.valueFilterBinders = new ArrayList<>(bindingDefs.size());
        this.mergedStrongBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        this.mergedValueRouteBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        this.boundIdBinder = null;
        this.selfFields = new ArrayList<>(bindingDefs.size());
        this.joinType = JoinType.UNION;
        String fieldErrorMsg = "The field configured for @Binding does not exist within the entity! type: {}, field: {}";

        for (BindingDef bindingDef : bindingDefs) {
            resetBindingDef(bindingDef);
            BindingType bindingType = determineBindingType(bindingDef);
            Processor processor = newProcessor(bindingDef);

            if (bindingType == BindingType.VALUE_ROUTE) {
                BindEndpoint bindEndpoint = newBindEndpoint(bindingDef);
                ValueRouteBinder valueRouteBinder = new ValueRouteBinder(bindingDef, null, bindEndpoint, processor);
                allBinders.add(valueRouteBinder);
                valueRouteBinders.add(valueRouteBinder);

                String belongAccessPath = valueRouteBinder.getBelongAccessPath();
                List<Binder> valueRouteBinders = mergedValueRouteBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                valueRouteBinders.add(valueRouteBinder);
                continue;
            }

            String field = bindingDef.getField();
            FieldDefinition fieldDefinition = entityElement.getFieldDefinition(field);
            Assert.notNull(fieldDefinition, fieldErrorMsg, genericType.getName(), field);
            FieldEndpoint fieldEndpoint = new FieldEndpoint(fieldDefinition, "#entity." + field);

            if (bindingType == BindingType.STRONG) {
                BindEndpoint bindEndpoint = newBindEndpoint(bindingDef);
                StrongBinder strongBinder = new StrongBinder(bindingDef, fieldEndpoint, bindEndpoint, processor);
                allBinders.add(strongBinder);
                strongBinders.add(strongBinder);

                String belongAccessPath = strongBinder.getBelongAccessPath();
                List<Binder> strongBinders = mergedStrongBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                strongBinders.add(strongBinder);

                if (strongBinder.isSameType() && primaryKey.equals(field)) {
                    if (entityDef.getPriority() == 0) {
                        entityDef.setPriority(-1);
                    }
                    boundIdBinder = strongBinder;
                }
                selfFields.add(field);

            } else if (bindingType == BindingType.WEAK) {
                WeakBinder weakBinder = new WeakBinder(bindingDef, fieldEndpoint, null, processor);
                allBinders.add(weakBinder);
                weakBinders.add(weakBinder);

            } else if (bindingType == BindingType.VALUE_FILTER) {
                ValueFilterBinder valueFilterBinder = new ValueFilterBinder(bindingDef, fieldEndpoint, null, processor);
                allBinders.add(valueFilterBinder);
                valueFilterBinders.add(valueFilterBinder);
            }
        }

        mergedStrongBindersMap = Collections.unmodifiableMap(mergedStrongBindersMap);
        mergedValueRouteBindersMap = Collections.unmodifiableMap(mergedValueRouteBindersMap);
        selfFields = Collections.unmodifiableList(selfFields);

        if (mergedStrongBindersMap.size() == 1 && mergedStrongBindersMap.containsKey("/")) {
            List<Binder> binders = mergedStrongBindersMap.get("/");
            boolean hasCollection = CollUtil.findOne(binders, b -> ((AbstractBinder) b).isBindCollection()) != null;
            if (!hasCollection) {
                joinType = binders.size() == 1 ? JoinType.SINGLE : JoinType.MULTI;
            }
        }
    }

    private void resetBindingDef(BindingDef bindingDef) {
        String field = StrUtil.trim(bindingDef.getField());
        String value = StrUtil.trim(bindingDef.getValue());
        String bind = StrUtil.trim(bindingDef.getBind());
        String expression = StrUtil.trim(bindingDef.getExpression());
        Class<?> processor = bindingDef.getProcessor();
        String bindField = StrUtil.trim(bindingDef.getBindField());

        // 兼容以往版本
        if (bind.startsWith("/")) {
            bind = StrUtil.removePrefix(bind, "/");
        }
        if (bind.startsWith("./")) {
            bind = StrUtil.removePrefix(bind, "./");
        }
        if (StringUtils.isNotBlank(bind) && StringUtils.isNotBlank(expression)) {
            Assert.notEmpty(bindField, "The bindField of @Binding cannot be empty!");
        }
        if (StringUtils.isNotBlank(bind) && StringUtils.isBlank(bindField)) {
            bindField = bind;
        }
        if (StringUtils.isNotBlank(expression) && processor == Object.class) {
            processor = SpELProcessor.class;
        }

        bindingDef.setField(field);
        bindingDef.setValue(value);
        bindingDef.setBind(bind);
        bindingDef.setExpression(expression);
        bindingDef.setProcessor(processor);
        bindingDef.setBindField(bindField);
    }

    private BindingType determineBindingType(BindingDef bindingDef) {
        String field = bindingDef.getField();
        String value = bindingDef.getValue();
        String bind = bindingDef.getBind();
        String expression = bindingDef.getExpression();
        if (ObjectUtil.isAllNotEmpty(field, bind)) {
            return BindingType.STRONG;

        } else if (ObjectUtil.isAllNotEmpty(field, expression)) {
            return BindingType.WEAK;

        } else if (ObjectUtil.isAllNotEmpty(value, bind)) {
            return BindingType.VALUE_ROUTE;

        } else if (ObjectUtil.isAllNotEmpty(field, value)) {
            return BindingType.VALUE_FILTER;
        }
        throw new RuntimeException("Unknown binding type!");
    }

    private Processor newProcessor(BindingDef bindingDef) {
        Assert.notNull(bindingDef, "The bindingDef cannot be null!");
        Class<?> processorClass = bindingDef.getProcessor();
        if (processorClass == Object.class) {
            return null;

        } else if (processorClass == SpELProcessor.class) {
            return new SpELProcessor(bindingDef);

        } else {
            ApplicationContext applicationContext = repository.getApplicationContext();
            String[] beanNamesForType = applicationContext.getBeanNamesForType(processorClass);
            if (beanNamesForType.length > 0) {
                return (Processor) applicationContext.getBean(beanNamesForType[0]);
            } else {
                return (Processor) ReflectUtil.newInstance(processorClass);
            }
        }
    }

    private BindEndpoint newBindEndpoint(BindingDef bindingDef) {
        String bind = bindingDef.getBind();

        RepositoryItem rootRepository = repository.getRootRepository();
        EntityElement entityElement = rootRepository.getEntityElement();
        FieldDefinition fieldDefinition = entityElement.getFieldDefinition(bind);
        Assert.notNull(fieldDefinition, "The bound property chain cannot be null! bind: {}", bind);
        BindEndpoint bindEndpoint = new BindEndpoint(fieldDefinition, "#entity." + bind);

        Map<String, RepositoryItem> repositoryMap = repository.getRepositoryMap();
        RepositoryItem belongRepository = repositoryMap.getOrDefault("/" + bind, rootRepository);
        belongRepository.setBound(true);

        bindEndpoint.setBelongAccessPath(belongRepository.getAccessPath());
        bindEndpoint.setBelongRepository(belongRepository);
        return bindEndpoint;
    }

    public List<Binder> getRootStrongBinders() {
        return mergedStrongBindersMap.get("/");
    }

    @Override
    public boolean hasValueRouteBinders() {
        return !getValueRouteBinders().isEmpty();
    }

    @Override
    public void appendFilterValue(Context context, Example example) {
        for (Binder valueFilterBinder : valueFilterBinders) {
            Object boundValue = valueFilterBinder.getBoundValue(context, null);
            boundValue = valueFilterBinder.input(context, boundValue);
            if (boundValue != null) {
                String fieldName = valueFilterBinder.getFieldName();
                example.eq(fieldName, boundValue);
            }
        }
    }

    @Override
    public void getBoundValue(Context context, Object rootEntity, Collection<?> entities) {
        for (Object entity : entities) {
            for (Binder strongBinder : getStrongBinders()) {
                Object fieldValue = strongBinder.getFieldValue(context, entity);
                if (fieldValue == null) {
                    Object boundValue = strongBinder.getBoundValue(context, rootEntity);
                    if (boundValue != null) {
                        strongBinder.setFieldValue(context, entity, boundValue);
                    }
                }
            }
        }
    }

    @Override
    public void setBoundId(Context context, Object rootEntity, Object entity) {
        Binder boundIdBinder = getBoundIdBinder();
        if (boundIdBinder != null) {
            Object boundValue = boundIdBinder.getBoundValue(context, rootEntity);
            if (boundValue == null) {
                Object primaryKey = boundIdBinder.getFieldValue(context, entity);
                if (primaryKey != null) {
                    boundIdBinder.setBoundValue(context, rootEntity, primaryKey);
                }
            }
        }
    }
}
