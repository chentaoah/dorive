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

package com.gitee.dorive.core.impl.resolver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.ele.EntityElement;
import com.gitee.dorive.api.entity.ele.FieldElement;
import com.gitee.dorive.api.entity.ele.PropChain;
import com.gitee.dorive.api.impl.SpELPropProxy;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.enums.BindingType;
import com.gitee.dorive.core.entity.enums.JoinType;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.binder.*;
import com.gitee.dorive.core.impl.processor.SpELProcessor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;

    private List<Binder> allBinders;
    private List<StrongBinder> strongBinders;
    private List<WeakBinder> weakBinders;
    private List<ValueRouteBinder> valueRouteBinders;
    private List<ValueFilterBinder> valueFilterBinders;
    // 决定了关联查询具体使用哪种实现
    private Map<String, List<StrongBinder>> mergedBindersMap;
    private StrongBinder boundIdBinder;
    private List<String> selfFields;
    private JoinType joinType;

    public BinderResolver(AbstractContextRepository<?, ?> repository) {
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
        this.mergedBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        this.boundIdBinder = null;
        this.selfFields = new ArrayList<>(bindingDefs.size());
        this.joinType = JoinType.UNION;
        String fieldErrorMsg = "The field configured for @Binding does not exist within the entity! type: {}, field: {}";

        for (BindingDef bindingDef : bindingDefs) {
            resetBindingDef(bindingDef);
            BindingType bindingType = determineBindingType(bindingDef);
            Processor processor = newProcessor(bindingDef);

            if (bindingType == BindingType.VALUE_ROUTE) {
                ValueRouteBinder valueRouteBinder = new ValueRouteBinder(bindingDef, processor);
                initBoundBinder(bindingDef, valueRouteBinder);
                allBinders.add(valueRouteBinder);
                valueRouteBinders.add(valueRouteBinder);
                continue;
            }

            String field = bindingDef.getField();
            String alias = entityElement.toAlias(field);

            FieldElement fieldElement = entityElement.getFieldElement(field);
            PropProxy propProxy = SpELPropProxy.newPropProxy("#entity." + field);
            PropChain fieldPropChain = new PropChain(fieldElement, propProxy);
            Assert.notNull(fieldPropChain, fieldErrorMsg, genericType.getName(), field);

            if (bindingType == BindingType.STRONG) {
                StrongBinder strongBinder = new StrongBinder(bindingDef, processor, fieldPropChain, alias);
                BoundBinder boundBinder = strongBinder.getBoundBinder();
                initBoundBinder(bindingDef, boundBinder);
                allBinders.add(strongBinder);
                strongBinders.add(strongBinder);

                String belongAccessPath = boundBinder.getBelongAccessPath();
                List<StrongBinder> strongBinders = mergedBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                strongBinders.add(strongBinder);

                if (strongBinder.isSameType() && primaryKey.equals(field)) {
                    if (entityDef.getPriority() == 0) {
                        entityDef.setPriority(-1);
                    }
                    boundIdBinder = strongBinder;
                }
                selfFields.add(field);

            } else if (bindingType == BindingType.WEAK) {
                WeakBinder weakBinder = new WeakBinder(bindingDef, processor, fieldPropChain, alias);
                allBinders.add(weakBinder);
                weakBinders.add(weakBinder);

            } else if (bindingType == BindingType.VALUE_FILTER) {
                ValueFilterBinder valueFilterBinder = new ValueFilterBinder(bindingDef, processor, fieldPropChain, alias);
                allBinders.add(valueFilterBinder);
                valueFilterBinders.add(valueFilterBinder);
            }
        }

        selfFields = Collections.unmodifiableList(selfFields);

        if (mergedBindersMap.size() == 1 && mergedBindersMap.containsKey("/")) {
            List<StrongBinder> binders = mergedBindersMap.get("/");
            boolean hasCollection = CollUtil.findOne(binders, StrongBinder::isCollection) != null;
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

    private void initBoundBinder(BindingDef bindingDef, BoundBinder boundBinder) {
        String bind = bindingDef.getBind();
        String bindField = bindingDef.getBindField();

        CommonRepository rootRepository = repository.getRootRepository();
        EntityElement entityElement = rootRepository.getEntityElement();
        FieldElement fieldElement = entityElement.getFieldElement(bind);
        PropProxy propProxy = SpELPropProxy.newPropProxy("#entity." + bind);
        PropChain boundPropChain = new PropChain(fieldElement, propProxy);
        Assert.notNull(boundPropChain, "The bound property chain cannot be null! bind: {}", bind);

        Map<String, CommonRepository> repositoryMap = repository.getRepositoryMap();
        CommonRepository belongRepository = repositoryMap.getOrDefault("/" + bind, rootRepository);
        belongRepository.setBound(true);
        EntityElement belongEntityElement = belongRepository.getEntityElement();
        String bindAlias = belongEntityElement.toAlias(bindField);

        boundBinder.setBoundPropChain(boundPropChain);
        boundBinder.setBelongAccessPath(belongRepository.getAccessPath());
        boundBinder.setBelongRepository(belongRepository);
        boundBinder.setBindAlias(bindAlias);
    }

    public void appendFilterValue(Context context, Example example) {
        for (ValueFilterBinder valueFilterBinder : valueFilterBinders) {
            Object boundValue = valueFilterBinder.getBoundValue(context, null);
            boundValue = valueFilterBinder.input(context, boundValue);
            if (boundValue != null) {
                String fieldName = valueFilterBinder.getFieldName();
                example.eq(fieldName, boundValue);
            }
        }
    }

}
