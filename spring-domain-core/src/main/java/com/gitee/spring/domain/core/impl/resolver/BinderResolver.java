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
package com.gitee.spring.domain.core.impl.resolver;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.api.Binder;
import com.gitee.spring.domain.core.api.Processor;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core.entity.definition.ProcessorDefinition;
import com.gitee.spring.domain.core.impl.processor.DefaultProcessor;
import com.gitee.spring.domain.core.impl.binder.ContextBinder;
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.core.util.PathUtils;
import com.gitee.spring.domain.core.util.ReflectUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;

    private List<Binder> allBinders;
    private List<PropertyBinder> propertyBinders;
    private List<ContextBinder> contextBinders;
    private String[] boundColumns;

    private List<Binder> boundValueBinders;
    private PropertyBinder boundIdBinder;

    public BinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveBinders(String accessPath, ElementDefinition elementDefinition) {
        allBinders = new ArrayList<>();
        propertyBinders = new ArrayList<>();
        contextBinders = new ArrayList<>();
        Set<String> boundColumns = new LinkedHashSet<>();

        List<BindingDefinition> bindingDefinitions = BindingDefinition.newBindingDefinitions(elementDefinition);
        for (BindingDefinition bindingDefinition : bindingDefinitions) {
            renewBindingDefinition(accessPath, bindingDefinition);

            Class<?> processorClass = bindingDefinition.getProcessor();
            ProcessorDefinition processorDefinition = ProcessorDefinition.newProcessorDefinition(processorClass);
            Processor processor = null;
            if (processorClass == DefaultProcessor.class) {
                processor = new DefaultProcessor();

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
                defaultProcessor.setProcessorDefinition(processorDefinition);
            }

            if (StringUtils.isNotBlank(bindingDefinition.getBindProp())) {
                PropertyBinder propertyBinder = newPropertyBinder(bindingDefinition, processor);
                allBinders.add(propertyBinder);
                propertyBinders.add(propertyBinder);
                boundColumns.add(StrUtil.toUnderlineCase(bindingDefinition.getAlias()));

            } else {
                ContextBinder contextBinder = newContextBinder(bindingDefinition, processor);
                allBinders.add(contextBinder);
                contextBinders.add(contextBinder);
            }
        }

        this.boundColumns = boundColumns.toArray(new String[0]);
    }

    private void renewBindingDefinition(String accessPath, BindingDefinition bindingDefinition) {
        String field = bindingDefinition.getField();
        String bindProp = bindingDefinition.getBindProp();
        String bindCtx = bindingDefinition.getBindCtx();
        String alias = bindingDefinition.getAlias();
        String bindAlias = bindingDefinition.getBindAlias();
        Class<?> processor = bindingDefinition.getProcessor();

        Assert.notBlank(bindProp + bindCtx, "The bindProp and bindCtx cannot be blank at the same time!");

        if (StringUtils.isBlank(alias)) {
            alias = field;
        }

        if (StringUtils.isNotBlank(bindProp)) {
            Assert.isTrue(bindProp.startsWith("/") || bindProp.startsWith("."), "The bindProp must be a path!");
            if (bindProp.startsWith(".")) {
                bindProp = PathUtils.getAbsolutePath(accessPath, bindProp);
            }
            if (StringUtils.isBlank(bindAlias)) {
                bindAlias = PathUtils.getFieldName(bindProp);
            }
        }

        bindingDefinition.setField(field);
        bindingDefinition.setBindProp(bindProp);
        bindingDefinition.setBindCtx(bindCtx);
        bindingDefinition.setAlias(alias);
        bindingDefinition.setBindAlias(bindAlias);
        bindingDefinition.setProcessor(processor);
    }

    private PropertyBinder newPropertyBinder(BindingDefinition bindingDefinition, Processor processor) {
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();
        String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), bindingDefinition.getBindProp());
        ConfiguredRepository belongRepository = allRepositoryMap.get(belongAccessPath);
        Assert.notNull(belongRepository, "The belong repository cannot be null!");
        belongRepository.setBoundEntity(true);

        Map<String, PropertyChain> allPropertyChainMap = repository.getPropertyResolver().getAllPropertyChainMap();
        PropertyChain boundPropertyChain = allPropertyChainMap.get(bindingDefinition.getBindProp());
        Assert.notNull(boundPropertyChain, "The bound property chain cannot be null!");
        boundPropertyChain.initialize();

        return new PropertyBinder(bindingDefinition, null, processor, belongAccessPath, belongRepository, boundPropertyChain);
    }

    private ContextBinder newContextBinder(BindingDefinition bindingDefinition, Processor processor) {
        return new ContextBinder(bindingDefinition, null, processor);
    }

}
