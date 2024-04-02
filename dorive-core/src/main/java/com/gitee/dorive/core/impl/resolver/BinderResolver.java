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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.def.BindingDef;
import com.gitee.dorive.api.def.OrderDef;
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.api.entity.PropChain;
import com.gitee.dorive.api.resolver.PropChainResolver;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.entity.option.BindingType;
import com.gitee.dorive.core.entity.option.JoinType;
import com.gitee.dorive.core.impl.binder.BoundBinder;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueBinder;
import com.gitee.dorive.core.impl.binder.WeakBinder;
import com.gitee.dorive.core.impl.processor.SpELProcessor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.PathUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;
    private PropChainResolver propChainResolver;

    private List<Binder> allBinders;
    private List<ValueBinder> valueBinders;
    private List<StrongBinder> strongBinders;
    // 决定了关联查询具体使用哪种实现
    private Map<String, List<StrongBinder>> mergedBindersMap;
    private StrongBinder boundIdBinder;
    private List<String> selfFields;
    private JoinType joinType;
    private List<WeakBinder> weakBinders;

    public BinderResolver(AbstractContextRepository<?, ?> repository, EntityEle entityEle) {
        this.repository = repository;
        this.propChainResolver = new PropChainResolver(entityEle.getEntityType());
    }

    public void resolve(String accessPath, OrderDef orderDef, EntityEle entityEle) {
        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();

        Class<?> genericType = entityEle.getGenericType();
        String idName = entityEle.getIdName();
        List<BindingDef> bindingDefs = entityEle.getBindingDefs();

        this.allBinders = new ArrayList<>(bindingDefs.size());
        this.valueBinders = new ArrayList<>(bindingDefs.size());
        this.strongBinders = new ArrayList<>(bindingDefs.size());
        this.mergedBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        this.boundIdBinder = null;
        this.selfFields = new ArrayList<>(bindingDefs.size());
        this.joinType = JoinType.UNION;
        this.weakBinders = new ArrayList<>(bindingDefs.size());
        String fieldErrorMsg = "The field configured for @Binding does not exist within the entity! type: {}, field: {}";

        for (BindingDef bindingDef : bindingDefs) {
            BindingType bindingType = determineBindingType(bindingDef);
            bindingDef = renewBindingDef(accessPath, bindingDef);
            Processor processor = newProcessor(bindingDef);

            if (bindingType == BindingType.VALUE) {
                ValueBinder valueBinder = new ValueBinder(bindingDef, processor);
                initBoundBinder(bindingDef, valueBinder);
                allBinders.add(valueBinder);
                valueBinders.add(valueBinder);
                continue;
            }

            String field = bindingDef.getField();
            String alias = entityEle.toAlias(field);

            PropChain fieldPropChain = propChainMap.get("/" + field);
            Assert.notNull(fieldPropChain, fieldErrorMsg, genericType.getName(), field);
            fieldPropChain.newPropProxy();

            if (bindingType == BindingType.STRONG) {
                StrongBinder strongBinder = new StrongBinder(bindingDef, processor, fieldPropChain, alias);
                BoundBinder boundBinder = strongBinder.getBoundBinder();
                initBoundBinder(bindingDef, boundBinder);
                allBinders.add(strongBinder);
                strongBinders.add(strongBinder);

                String belongAccessPath = boundBinder.getBelongAccessPath();
                List<StrongBinder> strongBinders = mergedBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                strongBinders.add(strongBinder);

                if (strongBinder.isSameType() && idName.equals(field)) {
                    if (orderDef.getPriority() == 0) {
                        orderDef.setPriority(-1);
                    }
                    boundIdBinder = strongBinder;
                }
                selfFields.add(field);

            } else if (bindingType == BindingType.WEAK) {
                WeakBinder weakBinder = new WeakBinder(bindingDef, processor, fieldPropChain, alias);
                allBinders.add(weakBinder);
                weakBinders.add(weakBinder);
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

    private BindingType determineBindingType(BindingDef bindingDef) {
        String field = StrUtil.trim(bindingDef.getField());
        String value = StrUtil.trim(bindingDef.getValue());
        String bindExp = StrUtil.trim(bindingDef.getBindExp());
        String processExp = StrUtil.trim(bindingDef.getProcessExp());
        if (ObjectUtil.isAllNotEmpty(field, bindExp)) {
            return BindingType.STRONG;

        } else if (ObjectUtil.isAllNotEmpty(field, processExp)) {
            return BindingType.WEAK;

        } else if (ObjectUtil.isAllNotEmpty(value, bindExp)) {
            return BindingType.VALUE;
        }
        throw new RuntimeException("Unknown binding type!");
    }

    private BindingDef renewBindingDef(String accessPath, BindingDef bindingDef) {
        bindingDef = BeanUtil.copyProperties(bindingDef, BindingDef.class);
        String field = StrUtil.trim(bindingDef.getField());
        String value = StrUtil.trim(bindingDef.getValue());
        String bindExp = StrUtil.trim(bindingDef.getBindExp());
        String processExp = StrUtil.trim(bindingDef.getProcessExp());
        Class<?> processor = bindingDef.getProcessor();
        String bindField = StrUtil.trim(bindingDef.getBindField());

        if (bindExp.startsWith(".")) {
            bindExp = PathUtils.getAbsolutePath(accessPath, bindExp);
        }
        if (StringUtils.isNotBlank(bindExp)) {
            if (StringUtils.isBlank(processExp) && StringUtils.isBlank(bindField)) {
                bindField = PathUtils.getLastName(bindExp);
            }
            Assert.notEmpty(bindField, "The bindField of @Binding cannot be empty!");
        }
        if (StringUtils.isNotBlank(processExp) && processor == Object.class) {
            processor = SpELProcessor.class;
        }

        bindingDef.setField(field);
        bindingDef.setValue(value);
        bindingDef.setBindExp(bindExp);
        bindingDef.setProcessExp(processExp);
        bindingDef.setProcessor(processor);
        bindingDef.setBindField(bindField);
        return bindingDef;
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
        String bindExp = bindingDef.getBindExp();
        String bindField = bindingDef.getBindField();

        Map<String, CommonRepository> repositoryMap = repository.getRepositoryMap();
        String belongAccessPath = PathUtils.getBelongPath(repositoryMap.keySet(), bindExp);
        CommonRepository belongRepository = repositoryMap.get(belongAccessPath);
        Assert.notNull(belongRepository, "The belong repository cannot be null! bindExp: {}", bindExp);
        belongRepository.setBoundEntity(true);

        PropChainResolver propChainResolver = repository.getPropChainResolver();
        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();
        PropChain boundPropChain = propChainMap.get(bindExp);
        Assert.notNull(boundPropChain, "The bound property chain cannot be null! bindExp: {}", bindExp);
        boundPropChain.newPropProxy();

        EntityEle entityEle = belongRepository.getEntityEle();
        String bindAlias = entityEle.toAlias(bindField);

        boundBinder.setBelongAccessPath(belongAccessPath);
        boundBinder.setBelongRepository(belongRepository);
        boundBinder.setBoundPropChain(boundPropChain);
        boundBinder.setBindAlias(bindAlias);
    }

}
