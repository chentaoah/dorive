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

package com.gitee.dorive.launcher.v1.impl.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.binder.enums.BindingType;
import com.gitee.dorive.base.v1.binder.enums.JoinType;
import com.gitee.dorive.base.v1.common.def.BindingDef;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.FieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.binder.AbstractBinder;
import com.gitee.dorive.binder.v1.impl.binder.StrongBinder;
import com.gitee.dorive.binder.v1.impl.binder.ValueFilterBinder;
import com.gitee.dorive.binder.v1.impl.binder.ValueRouteBinder;
import com.gitee.dorive.binder.v1.impl.binder.WeakBinder;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import com.gitee.dorive.binder.v1.impl.processor.SpELProcessor;
import com.gitee.dorive.binder.v1.impl.executor.DefaultBinderExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BinderExecutorBuilder {

    private RepositoryContext repositoryContext;
    private EntityElement entityElement;

    public BinderExecutor newBinderExecutor() {
        EntityDef entityDef = entityElement.getEntityDef();
        List<BindingDef> bindingDefs = entityElement.getBindingDefs();
        Class<?> genericType = entityElement.getGenericType();
        String primaryKey = entityElement.getPrimaryKey();

        List<Binder> allBinders = new ArrayList<>(bindingDefs.size());
        List<Binder> strongBinders = new ArrayList<>(bindingDefs.size());
        List<Binder> weakBinders = new ArrayList<>(bindingDefs.size());
        List<Binder> valueRouteBinders = new ArrayList<>(bindingDefs.size());
        List<Binder> valueFilterBinders = new ArrayList<>(bindingDefs.size());
        Map<String, List<Binder>> mergedStrongBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        Map<String, List<Binder>> mergedValueRouteBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        Binder boundIdBinder = null;
        List<String> selfFields = new ArrayList<>(bindingDefs.size());
        JoinType joinType = JoinType.UNION;

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
                List<Binder> targetValueRouteBinders = mergedValueRouteBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                targetValueRouteBinders.add(valueRouteBinder);
                continue;
            }

            String field = bindingDef.getField();
            FieldDefinition fieldDefinition = entityElement.getFieldDefinition(field);
            Assert.notNull(fieldDefinition, "The field configured for @Binding does not exist within the entity! type: {}, field: {}", genericType.getName(), field);
            FieldEndpoint fieldEndpoint = new FieldEndpoint(fieldDefinition, "#entity." + field);

            if (bindingType == BindingType.STRONG) {
                BindEndpoint bindEndpoint = newBindEndpoint(bindingDef);
                StrongBinder strongBinder = new StrongBinder(bindingDef, fieldEndpoint, bindEndpoint, processor);
                allBinders.add(strongBinder);
                strongBinders.add(strongBinder);

                String belongAccessPath = strongBinder.getBelongAccessPath();
                List<Binder> targetStrongBinders = mergedStrongBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                targetStrongBinders.add(strongBinder);

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

        return new DefaultBinderExecutor(allBinders, strongBinders, weakBinders, valueRouteBinders, valueFilterBinders,
                mergedStrongBindersMap, mergedValueRouteBindersMap,
                boundIdBinder, selfFields, joinType);
    }

    private void resetBindingDef(BindingDef bindingDef) {
        String field = StrUtil.trim(bindingDef.getField());
        String value = StrUtil.trim(bindingDef.getValue());
        String bind = StrUtil.trim(bindingDef.getBind());
        String expression = StrUtil.trim(bindingDef.getExpression());
        Class<?> processor = bindingDef.getProcessor();
        String targetField = StrUtil.trim(bindingDef.getTargetField());

        // 兼容以往版本
        if (bind.startsWith("/")) {
            bind = StrUtil.removePrefix(bind, "/");
        }
        if (bind.startsWith("./")) {
            bind = StrUtil.removePrefix(bind, "./");
        }

        if (StringUtils.isNotBlank(bind)) {
            if (StringUtils.isNotBlank(expression) && StringUtils.isBlank(targetField)) {
                throw new IllegalArgumentException("The targetField of @Binding cannot be empty!");

            } else if (StringUtils.isBlank(expression) && StringUtils.isNotBlank(targetField)) {
                RepositoryItem rootRepository = repositoryContext.getRootRepository();
                EntityElement entityElement = rootRepository.getEntityElement();
                FieldDefinition fieldDefinition = entityElement.getFieldDefinition(bind);
                expression = fieldDefinition.isCollection() ? "#val.![" + targetField + "]" : "#val." + targetField;

            } else if (StringUtils.isBlank(expression) && StringUtils.isBlank(targetField)) {
                targetField = bind;
            }
        }
        if (StringUtils.isNotBlank(expression) && processor == Object.class) {
            processor = SpELProcessor.class;
        }

        bindingDef.setField(field);
        bindingDef.setValue(value);
        bindingDef.setBind(bind);
        bindingDef.setExpression(expression);
        bindingDef.setProcessor(processor);
        bindingDef.setTargetField(targetField);
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
            ApplicationContext applicationContext = repositoryContext.getApplicationContext();
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

        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        EntityElement entityElement = rootRepository.getEntityElement();
        FieldDefinition fieldDefinition = entityElement.getFieldDefinition(bind);
        Assert.notNull(fieldDefinition, "The bound property chain cannot be null! bind: {}", bind);
        BindEndpoint bindEndpoint = new BindEndpoint(fieldDefinition, "#entity." + bind);

        Map<String, RepositoryItem> repositoryMap = repositoryContext.getRepositoryMap();
        RepositoryItem belongRepository = repositoryMap.getOrDefault("/" + bind, rootRepository);

        bindEndpoint.setBelongAccessPath(belongRepository.getAccessPath());
        bindEndpoint.setBelongRepository(belongRepository);
        return bindEndpoint;
    }

}
