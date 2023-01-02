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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.entity.definition.BindingDefinition;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.processor.DefaultProcessor;
import com.gitee.dorive.core.impl.processor.PropertyProcessor;
import com.gitee.dorive.core.api.Binder;
import com.gitee.dorive.core.api.Processor;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.PropertyChain;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import com.gitee.dorive.core.util.PathUtils;
import com.gitee.dorive.core.util.ReflectUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;

    private List<Binder> allBinders;
    private List<PropertyBinder> propertyBinders;
    private String[] boundColumns;
    private List<ContextBinder> contextBinders;
    private List<Binder> boundValueBinders;
    private PropertyBinder boundIdBinder;

    public BinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveAllBinders(String accessPath, EntityElement entityElement, EntityDefinition entityDefinition,
                                  String fieldPrefix, PropertyResolver propertyResolver) {

        List<BindingDefinition> bindingDefinitions = BindingDefinition.newBindingDefinitions(entityElement);
        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();

        allBinders = new ArrayList<>(bindingDefinitions.size());
        propertyBinders = new ArrayList<>(bindingDefinitions.size());
        Set<String> boundColumns = new LinkedHashSet<>(bindingDefinitions.size() * 4 / 3 + 1);
        contextBinders = new ArrayList<>(bindingDefinitions.size());
        boundValueBinders = new ArrayList<>(bindingDefinitions.size());
        boundIdBinder = null;

        for (BindingDefinition bindingDefinition : bindingDefinitions) {
            renewBindingDefinition(accessPath, bindingDefinition);

            String field = bindingDefinition.getField();
            PropertyChain fieldPropertyChain = allPropertyChainMap.get(fieldPrefix + field);
            Assert.notNull(fieldPropertyChain, "The field property chain cannot be null! entity: {}, field: {}",
                    entityElement.getGenericEntityClass().getSimpleName(), field);
            fieldPropertyChain.initialize();

            Processor processor = newProcessor(bindingDefinition);

            if (bindingDefinition.getBindExp().startsWith("/")) {
                PropertyBinder propertyBinder = newPropertyBinder(bindingDefinition, fieldPropertyChain, processor);
                allBinders.add(propertyBinder);
                propertyBinders.add(propertyBinder);
                boundColumns.add(StrUtil.toUnderlineCase(bindingDefinition.getAlias()));

                if (propertyBinder.isSameType()) {
                    if (!"id".equals(field)) {
                        boundValueBinders.add(propertyBinder);
                    } else {
                        if (entityDefinition.getOrder() == 0) {
                            entityDefinition.setOrder(-1);
                        }
                        boundIdBinder = propertyBinder;
                    }
                }

            } else {
                ContextBinder contextBinder = new ContextBinder(bindingDefinition, fieldPropertyChain, processor);
                allBinders.add(contextBinder);
                contextBinders.add(contextBinder);
                boundValueBinders.add(contextBinder);
            }
        }

        this.boundColumns = boundColumns.toArray(new String[0]);
    }

    private void renewBindingDefinition(String accessPath, BindingDefinition bindingDefinition) {
        String field = bindingDefinition.getField();
        String bindExp = bindingDefinition.getBindExp();
        String property = bindingDefinition.getProperty();
        Class<?> processor = bindingDefinition.getProcessor();
        String alias = bindingDefinition.getAlias();
        String bindAlias = bindingDefinition.getBindAlias();

        if (StringUtils.isBlank(alias)) {
            alias = field;
        }

        if (StringUtils.isBlank(bindAlias)) {
            bindAlias = property;
        }

        if (bindExp.startsWith("/") || bindExp.startsWith(".")) {
            if (bindExp.startsWith(".")) {
                bindExp = PathUtils.getAbsolutePath(accessPath, bindExp);
            }
            if (StringUtils.isBlank(bindAlias)) {
                bindAlias = PathUtils.getFieldName(bindExp);
            }
        }

        bindingDefinition.setField(field);
        bindingDefinition.setBindExp(bindExp);
        bindingDefinition.setProperty(property);
        bindingDefinition.setProcessor(processor);
        bindingDefinition.setAlias(alias);
        bindingDefinition.setBindAlias(bindAlias);
    }

    private Processor newProcessor(BindingDefinition bindingDefinition) {
        Class<?> processorClass = bindingDefinition.getProcessor();
        Processor processor = null;
        if (processorClass == DefaultProcessor.class) {
            if (StringUtils.isBlank(bindingDefinition.getProperty())) {
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
            defaultProcessor.setBindingDefinition(bindingDefinition);
        }
        if (processor instanceof PropertyProcessor) {
            Assert.notBlank(bindingDefinition.getProperty(), "The property of PropertyProcessor cannot be blank!");
        }
        return processor;
    }

    private PropertyBinder newPropertyBinder(BindingDefinition bindingDefinition, PropertyChain fieldPropertyChain, Processor processor) {
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();
        String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), bindingDefinition.getBindExp());
        ConfiguredRepository belongRepository = allRepositoryMap.get(belongAccessPath);
        Assert.notNull(belongRepository, "The belong repository cannot be null!");
        belongRepository.setBoundEntity(true);

        Map<String, PropertyChain> allPropertyChainMap = repository.getPropertyResolver().getAllPropertyChainMap();
        PropertyChain boundPropertyChain = allPropertyChainMap.get(bindingDefinition.getBindExp());
        Assert.notNull(boundPropertyChain, "The bound property chain cannot be null!");
        boundPropertyChain.initialize();

        return new PropertyBinder(bindingDefinition, fieldPropertyChain, processor, belongAccessPath, belongRepository, boundPropertyChain);
    }

}
