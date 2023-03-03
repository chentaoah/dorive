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
package com.gitee.dorive.core.repository;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.element.EntityElement;
import com.gitee.dorive.core.entity.element.PropertyChain;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.impl.AliasConverter;
import com.gitee.dorive.core.impl.OperationFactory;
import com.gitee.dorive.core.impl.executor.AdaptiveExecutor;
import com.gitee.dorive.core.impl.executor.ChainExecutor;
import com.gitee.dorive.core.impl.handler.AdaptiveEntityHandler;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.resolver.AdapterResolver;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DelegateResolver;
import com.gitee.dorive.core.impl.resolver.PropertyResolver;
import com.gitee.dorive.api.util.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
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
    protected AdapterResolver adapterResolver = new AdapterResolver(this);
    protected PropertyResolver propertyResolver = new PropertyResolver(false);

    protected Map<String, CommonRepository> allRepositoryMap = new LinkedHashMap<>();
    protected CommonRepository rootRepository;
    protected List<CommonRepository> subRepositories = new ArrayList<>();
    protected List<CommonRepository> orderedRepositories = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        entityClass = ReflectUtils.getFirstArgumentType(this.getClass());

        delegateResolver.resolveDelegateRepositoryMap();
        adapterResolver.resolveAdapter();

        List<Class<?>> allClasses = ReflectUtils.getAllSuperclasses(entityClass, Object.class);
        allClasses.add(entityClass);
        allClasses.forEach(clazz -> propertyResolver.resolveProperties(clazz));

        CommonRepository rootRepository = newRepository("/", entityClass);
        allRepositoryMap.put("/", rootRepository);
        this.rootRepository = rootRepository;
        orderedRepositories.add(rootRepository);

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        allPropertyChainMap.forEach((accessPath, propertyChain) -> {
            if (propertyChain.isAnnotatedEntity()) {
                CommonRepository subRepository = newRepository(accessPath, propertyChain.getDeclaredField());
                allRepositoryMap.put(accessPath, subRepository);
                subRepositories.add(subRepository);
                orderedRepositories.add(subRepository);
            }
        });

        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityDefinition().getOrder()));

        setEntityDefinition(rootRepository.getEntityDefinition());
        setEntityElement(rootRepository.getEntityElement());
        setOperationFactory(rootRepository.getOperationFactory());

        EntityHandler batchEntityHandler = new BatchEntityHandler(this, rootRepository.getOperationFactory());
        EntityHandler entityHandler = batchEntityHandler;
        if (delegateResolver.isDelegated()) {
            entityHandler = new AdaptiveEntityHandler(this, entityHandler);
        }
        Executor executor = new ChainExecutor(this, entityHandler);
        if (adapterResolver.isAdaptive()) {
            executor = new AdaptiveExecutor(this, executor);
        }
        setExecutor(executor);

        postProcessEntityClass(this, batchEntityHandler, entityClass);
    }

    @SuppressWarnings("unchecked")
    private CommonRepository newRepository(String accessPath, AnnotatedElement annotatedElement) {
        EntityDefinition entityDefinition = EntityDefinition.newEntityDefinition(annotatedElement);
        if (entityDefinition == null) {
            throw new RuntimeException("The Entity definition is null!");
        }
        EntityElement entityElement = EntityElement.newEntityElement(annotatedElement);

        if (annotatedElement instanceof Field) {
            Class<?> genericType = entityElement.getGenericType();
            EntityDefinition genericEntityDefinition = EntityDefinition.newEntityDefinition(genericType);
            if (genericEntityDefinition != null) {
                entityDefinition.merge(genericEntityDefinition);
            }
        }

        OperationFactory operationFactory = new OperationFactory(entityElement);

        Class<?> repositoryClass = entityDefinition.getRepository();
        Object repository;
        if (repositoryClass == DefaultRepository.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setEntityElement(entityElement);
            defaultRepository.setOperationFactory(operationFactory);
            defaultRepository.setExecutor(newExecutor(entityDefinition, entityElement));
        }

        boolean isRoot = "/".equals(accessPath);
        boolean aggregated = !(repository instanceof DefaultRepository);

        repository = postProcessRepository((AbstractRepository<Object, Object>) repository);

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        PropertyChain anchorPoint = allPropertyChainMap.get(accessPath);

        PropertyResolver propertyResolver = new PropertyResolver(true);
        String lastAccessPath = isRoot || entityElement.isCollection() ? "" : accessPath;
        propertyResolver.resolveProperties(lastAccessPath, entityElement.getGenericType());

        OrderBy defaultOrderBy = entityElement.newDefaultOrderBy(entityDefinition);

        BinderResolver binderResolver = new BinderResolver(this);
        String fieldPrefix = lastAccessPath + "/";
        binderResolver.resolveAllBinders(accessPath, entityElement, entityDefinition, fieldPrefix, propertyResolver);

        AliasConverter aliasConverter = new AliasConverter(entityElement);

        CommonRepository commonRepository = new CommonRepository();
        commonRepository.setEntityDefinition(entityDefinition);
        commonRepository.setEntityElement(entityElement);
        commonRepository.setOperationFactory(operationFactory);
        commonRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        commonRepository.setAccessPath(accessPath);
        commonRepository.setRoot(isRoot);
        commonRepository.setAggregated(aggregated);
        commonRepository.setAnchorPoint(anchorPoint);
        commonRepository.setPropertyResolver(propertyResolver);
        commonRepository.setDefaultOrderBy(defaultOrderBy);
        commonRepository.setFieldPrefix(fieldPrefix);
        commonRepository.setBinderResolver(binderResolver);
        commonRepository.setBoundEntity(false);
        commonRepository.setAliasConverter(aliasConverter);
        return commonRepository;
    }

    protected abstract Executor newExecutor(EntityDefinition entityDefinition, EntityElement entityElement);

    protected abstract AbstractRepository<Object, Object> postProcessRepository(AbstractRepository<Object, Object> repository);

    protected abstract void postProcessEntityClass(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler, Class<?> entityClass);

}
