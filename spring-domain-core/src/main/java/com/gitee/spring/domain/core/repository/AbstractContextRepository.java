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
package com.gitee.spring.domain.core.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.api.EntityHandler;
import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.api.constant.Order;
import com.gitee.spring.domain.core.entity.EntityElement;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.entity.executor.OrderBy;
import com.gitee.spring.domain.core.impl.executor.ChainExecutor;
import com.gitee.spring.domain.core.impl.handler.AdaptiveEntityHandler;
import com.gitee.spring.domain.core.impl.handler.BatchEntityHandler;
import com.gitee.spring.domain.core.impl.resolver.*;
import com.gitee.spring.domain.core.util.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AnnotatedElement;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected Class<?> entityClass;

    protected DelegateResolver delegateResolver = new DelegateResolver(this);
    protected PropertyResolver propertyResolver = new PropertyResolver();

    protected Map<String, ConfiguredRepository> allRepositoryMap = new LinkedHashMap<>();
    protected ConfiguredRepository rootRepository;
    protected List<ConfiguredRepository> subRepositories = new ArrayList<>();
    protected List<ConfiguredRepository> orderedRepositories = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        entityClass = ReflectUtils.getFirstArgumentType(this.getClass());
        delegateResolver.resolveDelegateRepositoryMap();
        propertyResolver.resolveAllPropertyChainMap(entityClass);

        ConfiguredRepository rootRepository = newRepository("/", entityClass);
        allRepositoryMap.put("/", rootRepository);
        this.rootRepository = rootRepository;
        orderedRepositories.add(rootRepository);

        setEntityElement(rootRepository.getEntityElement());
        setEntityDefinition(rootRepository.getEntityDefinition());

        EntityHandler entityHandler = new BatchEntityHandler(this);
        if (delegateResolver.isDelegated()) {
            entityHandler = new AdaptiveEntityHandler(this, entityHandler);
        }
        setExecutor(new ChainExecutor(this, entityHandler));

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        allPropertyChainMap.forEach((accessPath, propertyChain) -> {
            if (propertyChain.isAnnotatedEntity()) {
                ConfiguredRepository subRepository = newRepository(accessPath, propertyChain.getProperty().getDeclaredField());
                allRepositoryMap.put(accessPath, subRepository);
                subRepositories.add(subRepository);
                orderedRepositories.add(subRepository);
            }
        });

        new RepoPropertyResolver(this).resolvePropertyChainMap();
        new RepoBinderResolver(this).resolveValueBinders();

        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityDefinition().getOrder()));
    }

    @SuppressWarnings("unchecked")
    private ConfiguredRepository newRepository(String accessPath, AnnotatedElement annotatedElement) {
        EntityElement entityElement = EntityElement.newEntityElement(annotatedElement);
        EntityDefinition entityDefinition = EntityDefinition.newEntityDefinition(entityElement);

        Class<?> repositoryClass = entityDefinition.getRepository();
        Object repository;
        if (repositoryClass == DefaultRepository.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityElement(entityElement);
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setExecutor(newExecutor(entityElement, entityDefinition));
        }

        OrderBy orderBy = null;
        String orderByAsc = entityDefinition.getOrderByAsc();
        if (StringUtils.isNotBlank(orderByAsc)) {
            orderByAsc = StrUtil.toUnderlineCase(orderByAsc);
            orderBy = new OrderBy(StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]), Order.ASC);
        }
        String orderByDesc = entityDefinition.getOrderByDesc();
        if (StringUtils.isNotBlank(orderByDesc)) {
            orderByDesc = StrUtil.toUnderlineCase(orderByDesc);
            orderBy = new OrderBy(StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]), Order.DESC);
        }

        boolean aggregateRoot = "/".equals(accessPath);
        boolean aggregated = !(repository instanceof DefaultRepository);
        repository = postProcessRepository((AbstractRepository<Object, Object>) repository);

        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolveBinders(accessPath, entityElement);

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        PropertyChain propertyChain = allPropertyChainMap.get(accessPath);

        String fieldPrefix = aggregateRoot ? "/" : accessPath + "/";

        ConfiguredRepository configuredRepository = new ConfiguredRepository();
        configuredRepository.setEntityElement(entityElement);
        configuredRepository.setEntityDefinition(entityDefinition);
        configuredRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        configuredRepository.setAccessPath(accessPath);
        configuredRepository.setAggregateRoot(aggregateRoot);
        configuredRepository.setAggregated(aggregated);
        configuredRepository.setOrderBy(orderBy);
        configuredRepository.setBinderResolver(binderResolver);
        configuredRepository.setBoundEntity(false);
        configuredRepository.setAnchorPoint(propertyChain);
        configuredRepository.setFieldPrefix(fieldPrefix);
        configuredRepository.setPropertyChainMap(new LinkedHashMap<>());
        return configuredRepository;
    }

    protected abstract Executor newExecutor(EntityElement entityElement, EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> postProcessRepository(AbstractRepository<Object, Object> repository);

}
