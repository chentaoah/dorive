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
import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.impl.resolver.PropChainResolver;
import com.gitee.dorive.core.api.Binder;
import com.gitee.dorive.core.api.Processor;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.processor.DefaultProcessor;
import com.gitee.dorive.core.impl.processor.PropertyProcessor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.PathUtils;
import com.gitee.dorive.api.util.ReflectUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;

    private List<Binder> allBinders;
    private List<PropertyBinder> propertyBinders;
    private List<String> boundFields;
    private List<ContextBinder> contextBinders;
    private List<Binder> boundValueBinders;
    private PropertyBinder boundIdBinder;

    public BinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolve(String accessPath, EntityDef entityDef, EntityEle entityEle, PropChainResolver propChainResolver) {
        List<BindingDef> bindingDefs = entityEle.getBindingDefs();
        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();

        allBinders = new ArrayList<>(bindingDefs.size());
        propertyBinders = new ArrayList<>(bindingDefs.size());
        boundFields = new ArrayList<>(bindingDefs.size());
        contextBinders = new ArrayList<>(bindingDefs.size());
        boundValueBinders = new ArrayList<>(bindingDefs.size());
        boundIdBinder = null;

        for (BindingDef bindingDef : bindingDefs) {
            bindingDef = renewBindingDef(accessPath, bindingDef);

            String field = bindingDef.getField();
            String alias = entityEle.toAlias(field);

            PropChain fieldPropChain = propChainMap.get("/" + field);
            Assert.notNull(fieldPropChain, "The field of @Binding may be wrong! type: {}, field: {}", entityEle.getGenericType().getName(), field);
            fieldPropChain.newPropProxy();

            Processor processor = newProcessor(bindingDef);

            if (bindingDef.getBindExp().startsWith("/")) {
                PropertyBinder propertyBinder = newPropertyBinder(bindingDef, fieldPropChain, processor, alias);
                allBinders.add(propertyBinder);
                propertyBinders.add(propertyBinder);
                boundFields.add(bindingDef.getField());

                if (propertyBinder.isSameType()) {
                    if (!"id".equals(field)) {
                        boundValueBinders.add(propertyBinder);
                    } else {
                        if (entityDef.getPriority() == 0) {
                            entityDef.setPriority(-1);
                        }
                        boundIdBinder = propertyBinder;
                    }
                }

            } else {
                ContextBinder contextBinder = new ContextBinder(bindingDef, fieldPropChain, processor, alias);
                allBinders.add(contextBinder);
                contextBinders.add(contextBinder);
                boundValueBinders.add(contextBinder);
            }
        }
    }

    private BindingDef renewBindingDef(String accessPath, BindingDef bindingDef) {
        bindingDef = BeanUtil.copyProperties(bindingDef, BindingDef.class);
        String bindExp = bindingDef.getBindExp();
        if (bindExp.startsWith(".")) {
            bindExp = PathUtils.getAbsolutePath(accessPath, bindExp);
            bindingDef.setBindExp(bindExp);
        }
        return bindingDef;
    }

    private Processor newProcessor(BindingDef bindingDef) {
        Class<?> processorClass = bindingDef.getProcessor();
        Processor processor = null;
        if (processorClass == Object.class) {
            if (StringUtils.isBlank(bindingDef.getProperty())) {
                processor = new DefaultProcessor();
            } else {
                processor = new PropertyProcessor();
            }
        } else {
            ApplicationContext applicationContext = repository.getApplicationContext();
            String[] beanNamesForType = applicationContext.getBeanNamesForType(processorClass);
            if (beanNamesForType.length > 0) {
                processor = (Processor) applicationContext.getBean(beanNamesForType[0]);
            }
            if (processor == null) {
                processor = (Processor) ReflectUtils.newInstance(processorClass);
            }
        }
        if (processor instanceof DefaultProcessor) {
            DefaultProcessor defaultProcessor = (DefaultProcessor) processor;
            defaultProcessor.setBindingDef(bindingDef);
        }
        if (processor instanceof PropertyProcessor) {
            Assert.notBlank(bindingDef.getProperty(), "The property of PropertyProcessor cannot be blank!");
        }
        return processor;
    }

    private PropertyBinder newPropertyBinder(BindingDef bindingDef, PropChain fieldPropChain, Processor processor, String alias) {
        String bindExp = bindingDef.getBindExp();
        String property = bindingDef.getProperty();

        Map<String, CommonRepository> repositoryMap = repository.getRepositoryMap();
        String belongAccessPath = PathUtils.getBelongPath(repositoryMap.keySet(), bindExp);
        CommonRepository belongRepository = repositoryMap.get(belongAccessPath);
        Assert.notNull(belongRepository, "The belong repository cannot be null!");
        belongRepository.setBoundEntity(true);

        Map<String, PropChain> propChainMap = repository.getPropChainResolver().getPropChainMap();
        PropChain boundPropChain = propChainMap.get(bindExp);
        Assert.notNull(boundPropChain, "The bound property chain cannot be null!");
        boundPropChain.newPropProxy();

        EntityEle entityEle = belongRepository.getEntityEle();
        String fieldName = StringUtils.isNotBlank(property) ? property : PathUtils.getLastName(bindExp);
        String bindAlias = entityEle.toAlias(fieldName);

        return new PropertyBinder(bindingDef, fieldPropChain, processor, alias, belongAccessPath, belongRepository, boundPropChain, bindAlias);
    }

}
