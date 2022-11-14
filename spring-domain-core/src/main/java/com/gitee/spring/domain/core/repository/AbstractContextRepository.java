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

import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.impl.executor.AdaptiveExecutor;
import com.gitee.spring.domain.core.impl.executor.ChainExecutor;
import com.gitee.spring.domain.core.impl.handler.BatchEntityHandler;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core.impl.resolver.DelegateResolver;
import com.gitee.spring.domain.core.impl.resolver.PropertyResolver;
import com.gitee.spring.domain.core.impl.resolver.RepoBinderResolver;
import com.gitee.spring.domain.core.impl.resolver.RepoPropertyResolver;
import com.gitee.spring.domain.core.util.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;

        delegateResolver.resolveRepositoryMap();

        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.forEach(superClass -> propertyResolver.resolveProperties("", superClass));
        propertyResolver.resolveProperties("", entityClass);

        ConfiguredRepository rootRepository = newRepository("/", entityClass);
        allRepositoryMap.put("/", rootRepository);
        this.rootRepository = rootRepository;
        orderedRepositories.add(rootRepository);

        setElementDefinition(rootRepository.getElementDefinition());
        setEntityDefinition(rootRepository.getEntityDefinition());
        
        if (delegateResolver.isDelegated()) {
            setExecutor(new AdaptiveExecutor(this, new BatchEntityHandler(this)));
        } else {
            setExecutor(new ChainExecutor(this, new BatchEntityHandler(this)));
        }

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
        ElementDefinition elementDefinition = ElementDefinition.newElementDefinition(annotatedElement);
        EntityDefinition entityDefinition = EntityDefinition.newEntityDefinition(elementDefinition);

        Class<?> repositoryClass = entityDefinition.getRepository();
        Object repository;
        if (repositoryClass == DefaultRepository.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setElementDefinition(elementDefinition);
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setExecutor(newExecutor(elementDefinition, entityDefinition));
        }

        boolean aggregated = !(repository instanceof DefaultRepository);

        repository = postProcessRepository((AbstractRepository<Object, Object>) repository);

        boolean aggregateRoot = "/".equals(accessPath);

        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolveBinders(accessPath, elementDefinition);

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        PropertyChain propertyChain = allPropertyChainMap.get(accessPath);

        String fieldPrefix = aggregateRoot ? "/" : accessPath + "/";

        ConfiguredRepository configuredRepository = new ConfiguredRepository();
        configuredRepository.setElementDefinition(elementDefinition);
        configuredRepository.setEntityDefinition(entityDefinition);
        configuredRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        configuredRepository.setAggregated(aggregated);
        configuredRepository.setAggregateRoot(aggregateRoot);
        configuredRepository.setAccessPath(accessPath);
        configuredRepository.setBinderResolver(binderResolver);
        configuredRepository.setBoundEntity(false);
        configuredRepository.setAnchorPoint(propertyChain);
        configuredRepository.setFieldPrefix(fieldPrefix);
        configuredRepository.setPropertyChainMap(new LinkedHashMap<>());
        return configuredRepository;
    }

    protected abstract Executor newExecutor(ElementDefinition elementDefinition, EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> postProcessRepository(AbstractRepository<Object, Object> repository);

}
