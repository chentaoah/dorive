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

package com.gitee.dorive.repository.v1.impl.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.aggregate.api.EntityResolver;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.api.BoundedContext;
import com.gitee.dorive.base.v1.common.api.BoundedContextAware;
import com.gitee.dorive.base.v1.common.api.RepositoryPostProcessor;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.common.def.OrderByDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Matcher;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.core.impl.OrderByFactory;
import com.gitee.dorive.base.v1.core.util.ReflectUtils;
import com.gitee.dorive.base.v1.executor.api.*;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.impl.context.RepositoryContext;
import com.gitee.dorive.repository.v1.impl.resolver.DerivedRepositoryResolver;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

@Getter
@Setter
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK>
        implements ApplicationContextAware, BoundedContextAware, InitializingBean, com.gitee.dorive.base.v1.repository.api.RepositoryContext {

    private ApplicationContext applicationContext;
    private BoundedContext boundedContext;

    private RepositoryDef repositoryDef;
    private Map<String, RepositoryItem> repositoryMap = new LinkedHashMap<>();
    private RepositoryItem rootRepository;
    private List<RepositoryItem> subRepositories = new ArrayList<>();
    private List<RepositoryItem> orderedRepositories = new ArrayList<>();
    private DerivedRepositoryResolver derivedRepositoryResolver;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBoundedContext(BoundedContext boundedContext) {
        this.boundedContext = boundedContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class<?> repositoryClass = this.getClass();
        Class<?> entityClass = ReflectUtils.getFirstTypeArgument(repositoryClass);

        prepareRepositoryDef(repositoryClass, entityClass);
        Assert.notNull(repositoryDef, "The @Repository does not exist! type: {}", repositoryClass.getName());
        resetBoundedContextIfNecessary();

        EntityResolver entityResolver = applicationContext.getBean(EntityResolver.class);
        List<EntityElement> entityElements = entityResolver.resolve(entityClass);

        for (EntityElement entityElement : entityElements) {
            String accessPath = entityElement.getAccessPath();
            ProxyRepository repository = newRepository(entityElement);
            repositoryMap.put(accessPath, repository);
            if (repository.isRoot()) {
                rootRepository = repository;
            } else {
                subRepositories.add(repository);
            }
            orderedRepositories.add(repository);
        }
        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityElement().getEntityDef().getPriority()));

        setEntityElement(rootRepository.getEntityElement());
        setOperationFactory(rootRepository.getOperationFactory());
        setExecutor(newExecutor());
    }

    protected void prepareRepositoryDef(Class<?> repositoryClass, Class<?> entityClass) {
        this.repositoryDef = RepositoryDef.fromElement(repositoryClass);
        for (RepositoryPostProcessor postProcessor : RepositoryContext.getRepositoryPostProcessors()) {
            postProcessor.postProcessRepositoryDef(repositoryClass, entityClass, repositoryDef);
        }
    }

    protected void resetBoundedContextIfNecessary() {
        String boundedContextName = repositoryDef.getBoundedContext();
        if (StringUtils.isNotBlank(boundedContextName)) {
            if (applicationContext.containsBean(boundedContextName)) {
                this.boundedContext = applicationContext.getBean(boundedContextName, BoundedContext.class);
            }
        }
    }

    private ProxyRepository newRepository(EntityElement entityElement) {
        resetEntityDef(entityElement);

        OrderByDef orderByDef = entityElement.getOrderByDef();
        String accessPath = entityElement.getAccessPath();
        boolean isRoot = entityElement.isRoot();

        AbstractRepository<Object, Object> repository;
        if (isRoot) {
            repository = doNewRepository(entityElement);
            repository = processRepository(repository);
        } else {
            repository = doGetRepository(entityElement);
        }

        OperationFactory operationFactory = repository.getOperationFactory();
        boolean isAggregated = repository instanceof AbstractContextRepository;
        OrderByFactory orderByFactory = orderByDef == null ? null : new OrderByFactory(orderByDef);

        // 从上下文获取工厂
        RepositoryBuilder repositoryBuilder = applicationContext.getBean(RepositoryBuilder.class);
        BinderExecutor binderExecutor = repositoryBuilder.newBinderExecutor(this, entityElement);

        ProxyRepository repositoryWrapper = new ProxyRepository();
        repositoryWrapper.setEntityElement(entityElement);
        repositoryWrapper.setOperationFactory(operationFactory);
        repositoryWrapper.setProxyRepository(repository);
        repositoryWrapper.setAccessPath(accessPath);
        repositoryWrapper.setRoot(isRoot);
        repositoryWrapper.setAggregated(isAggregated);
        repositoryWrapper.setBinderExecutor(binderExecutor);
        repositoryWrapper.setOrderByFactory(orderByFactory);
        repositoryWrapper.setBound(false);
        // 匹配器
        Matcher matcher = repositoryBuilder.newAdaptiveMatcher(repositoryWrapper.isRoot(), repositoryWrapper.getName());
        repositoryWrapper.setMatcher(matcher);
        return repositoryWrapper;
    }

    private void resetEntityDef(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> genericType = entityElement.getGenericType();

        Class<?> repositoryClass = entityDef.getRepository();
        Class<?> newRepositoryClass;
        // 自定义
        if (repositoryClass != Object.class) {
            return;
        }
        if (entityElement.isRoot()) {
            newRepositoryClass = DefaultRepository.class;
        } else {
            newRepositoryClass = RepositoryContext.findRepositoryClass(genericType);
        }
        Assert.notNull(newRepositoryClass, "No type of repository found! type: {}", genericType.getName());
        entityDef.setRepository(newRepositoryClass);
    }

    protected AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository) {
        return repository;
    }

    @SuppressWarnings("unchecked")
    private AbstractRepository<Object, Object> doGetRepository(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> repositoryClass = entityDef.getRepository();
        AbstractRepository<Object, Object> repository = (AbstractRepository<Object, Object>) applicationContext.getBean(repositoryClass);
        if (!entityDef.isAggregate()) {
            AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) repository;
            RepositoryItem rootRepository = abstractContextRepository.getRootRepository();
            if (rootRepository instanceof ProxyRepository) {
                return ((ProxyRepository) rootRepository).getProxyRepository();
            }
        }
        return repository;
    }

    protected Executor newExecutor() {
        // 从上下文获取工厂
        RepositoryBuilder repositoryBuilder = applicationContext.getBean(RepositoryBuilder.class);
        ExecutorFactory executorFactory = applicationContext.getBean(ExecutorFactory.class);
        EntityHandlerFactory entityHandlerFactory = applicationContext.getBean(EntityHandlerFactory.class);
        EntityOpHandlerFactory entityOpHandlerFactory = applicationContext.getBean(EntityOpHandlerFactory.class);
        // 处理器
        EntityHandler entityHandler = newEntityHandler(repositoryBuilder);
        EntityOpHandler entityOpHandler = newEntityOpHandler(repositoryBuilder);
        // 委托
        derivedRepositoryResolver = new DerivedRepositoryResolver(this);
        derivedRepositoryResolver.resolve();
        if (derivedRepositoryResolver.hasDerived()) {
            entityHandler = entityHandlerFactory.create("DelegatedEntityHandler",
                    this, derivedRepositoryResolver.getEntityHandlerMap(entityHandler));
            entityOpHandler = entityOpHandlerFactory.create("DelegatedEntityOpHandler",
                    this, derivedRepositoryResolver.getEntityOpHandlerMap(entityOpHandler));
        }
        // 创建上下文执行器
        return executorFactory.create("ContextExecutor", this, entityHandler, entityOpHandler);
    }

    protected EntityHandler newEntityHandler(RepositoryBuilder repositoryBuilder) {
        return repositoryBuilder.newEntityHandler(this);
    }

    protected EntityOpHandler newEntityOpHandler(RepositoryBuilder repositoryBuilder) {
        return repositoryBuilder.newEntityOpHandler(this);
    }

    protected abstract DefaultRepository doNewRepository(EntityElement entityElement);

}
