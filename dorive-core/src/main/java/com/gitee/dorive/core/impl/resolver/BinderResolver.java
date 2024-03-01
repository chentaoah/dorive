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
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.api.impl.resolver.PropChainResolver;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.entity.option.JoinType;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.SpELProcessor;
import com.gitee.dorive.core.impl.binder.WeakBinder;
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
    private List<StrongBinder> strongBinders;
    private Map<String, List<StrongBinder>> mergedBindersMap;
    private StrongBinder boundIdBinder;
    private List<String> selfFields;
    private JoinType joinType;
    private List<WeakBinder> weakBinders;

    public BinderResolver(AbstractContextRepository<?, ?> repository, EntityEle entityEle) {
        this.repository = repository;
        this.propChainResolver = new PropChainResolver(entityEle.getEntityType());
    }

    public void resolve(String accessPath, EntityDef entityDef, EntityEle entityEle) {
        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();

        Class<?> genericType = entityEle.getGenericType();
        String idName = entityEle.getIdName();
        List<BindingDef> bindingDefs = entityEle.getBindingDefs();

        this.allBinders = new ArrayList<>(bindingDefs.size());
        this.strongBinders = new ArrayList<>(bindingDefs.size());
        this.mergedBindersMap = new LinkedHashMap<>(bindingDefs.size() * 4 / 3 + 1);
        this.boundIdBinder = null;
        this.selfFields = new ArrayList<>(bindingDefs.size());
        this.joinType = JoinType.UNION;
        this.weakBinders = new ArrayList<>(bindingDefs.size());
        String fieldErrorMsg = "The field configured for @Binding does not exist within the entity! type: {}, field: {}";

        for (BindingDef bindingDef : bindingDefs) {
            bindingDef = renewBindingDef(accessPath, bindingDef);
            String field = bindingDef.getField();
            String bindExp = bindingDef.getBindExp();

            String alias = entityEle.toAlias(field);

            PropChain fieldPropChain = propChainMap.get("/" + field);
            Assert.notNull(fieldPropChain, fieldErrorMsg, genericType.getName(), field);
            fieldPropChain.newPropProxy();

            Processor processor = newProcessor(bindingDef);

            if (StringUtils.isNotBlank(bindExp)) {
                StrongBinder strongBinder = newStrongBinder(bindingDef, alias, fieldPropChain, processor);
                allBinders.add(strongBinder);
                strongBinders.add(strongBinder);

                String belongAccessPath = strongBinder.getBelongAccessPath();
                List<StrongBinder> strongBinders = mergedBindersMap.computeIfAbsent(belongAccessPath, key -> new ArrayList<>(2));
                strongBinders.add(strongBinder);

                if (strongBinder.isSameType() && idName.equals(field)) {
                    if (entityDef.getPriority() == 0) {
                        entityDef.setPriority(-1);
                    }
                    boundIdBinder = strongBinder;
                }

                selfFields.add(field);

            } else {
                WeakBinder weakBinder = new WeakBinder(bindingDef, alias, fieldPropChain, processor);
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

    private BindingDef renewBindingDef(String accessPath, BindingDef bindingDef) {
        bindingDef = BeanUtil.copyProperties(bindingDef, BindingDef.class);
        String field = StrUtil.trim(bindingDef.getField());
        String bindExp = StrUtil.trim(bindingDef.getBindExp());
        String processExp = StrUtil.trim(bindingDef.getProcessExp());
        String bindField = StrUtil.trim(bindingDef.getBindField());
        Class<?> processor = bindingDef.getProcessor();
        Assert.notEmpty(field, "The field of @Binding cannot be empty!");
        Assert.notEmpty(bindExp + processExp, "The expression of @Binding cannot be empty!");

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
        bindingDef.setBindExp(bindExp);
        bindingDef.setProcessExp(processExp);
        bindingDef.setBindField(bindField);
        bindingDef.setProcessor(processor);
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

    private StrongBinder newStrongBinder(BindingDef bindingDef, String alias, PropChain fieldPropChain, Processor processor) {
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

        return new StrongBinder(bindingDef, alias, fieldPropChain, processor,
                belongAccessPath, belongRepository, boundPropChain, bindAlias);
    }

}
