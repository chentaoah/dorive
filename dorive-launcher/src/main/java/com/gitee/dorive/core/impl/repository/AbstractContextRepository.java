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

package com.gitee.dorive.core.impl.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.api.BoundedContext;
import com.gitee.dorive.base.v1.common.api.BoundedContextAware;
import com.gitee.dorive.base.v1.common.entity.EntityDefinition;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.common.def.OrderByDef;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.aggregate.v1.impl.EntityDefinitionResolver;
import com.gitee.dorive.aggregate.v1.impl.EntityElementResolver;
import com.gitee.dorive.aggregate.v1.util.ReflectUtils;
import com.gitee.dorive.base.v1.common.api.RepositoryPostProcessor;
import com.gitee.dorive.executor.v1.api.EntityHandler;
import com.gitee.dorive.executor.v1.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.core.config.RepositoryContext;
import com.gitee.dorive.executor.v1.impl.context.AdaptiveMatcher;
import com.gitee.dorive.core.impl.executor.ContextExecutor;
import com.gitee.dorive.executor.v1.impl.factory.OperationFactory;
import com.gitee.dorive.executor.v1.impl.factory.OrderByFactory;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.handler.DelegatedEntityHandler;
import com.gitee.dorive.core.impl.handler.op.BatchEntityOpHandler;
import com.gitee.dorive.core.impl.handler.op.DelegatedEntityOpHandler;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DerivedRepositoryResolver;
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
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, BoundedContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private BoundedContext boundedContext;

    private RepositoryDef repositoryDef;
    private Map<String, ProxyRepository> repositoryMap = new LinkedHashMap<>();
    private ProxyRepository rootRepository;
    private List<ProxyRepository> subRepositories = new ArrayList<>();
    private List<ProxyRepository> orderedRepositories = new ArrayList<>();
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

        EntityDefinitionResolver entityDefinitionResolver = new EntityDefinitionResolver();
        EntityDefinition entityDefinition = entityDefinitionResolver.resolve(entityClass);

        EntityElementResolver entityElementResolver = new EntityElementResolver();
        List<EntityElement> entityElements = entityElementResolver.resolve(entityDefinition);

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
        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolve(entityElement);
        OrderByFactory orderByFactory = orderByDef == null ? null : new OrderByFactory(orderByDef);

        ProxyRepository repositoryWrapper = new ProxyRepository();
        repositoryWrapper.setEntityElement(entityElement);
        repositoryWrapper.setOperationFactory(operationFactory);
        repositoryWrapper.setProxyRepository(repository);
        repositoryWrapper.setAccessPath(accessPath);
        repositoryWrapper.setRoot(isRoot);
        repositoryWrapper.setAggregated(isAggregated);
        repositoryWrapper.setBinderResolver(binderResolver);
        repositoryWrapper.setOrderByFactory(orderByFactory);
        repositoryWrapper.setBound(false);
        repositoryWrapper.setMatcher(new AdaptiveMatcher(repositoryWrapper.getName(), repositoryWrapper.isRoot()));
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
            return abstractContextRepository.getRootRepository().getProxyRepository();
        }
        return repository;
    }

    protected Executor newExecutor() {
        EntityHandler entityHandler = newEntityHandler();
        EntityOpHandler entityOpHandler = newEntityOpHandler();

        derivedRepositoryResolver = new DerivedRepositoryResolver(this);
        derivedRepositoryResolver.resolve();
        if (derivedRepositoryResolver.hasDerived()) {
            entityHandler = new DelegatedEntityHandler(this, derivedRepositoryResolver.getEntityHandlerMap(entityHandler));
            entityOpHandler = new DelegatedEntityOpHandler(this, derivedRepositoryResolver.getEntityOpHandlerMap(entityOpHandler));
        }

        return new ContextExecutor(this, entityHandler, entityOpHandler);
    }

    protected EntityHandler newEntityHandler() {
        return new BatchEntityHandler(this);
    }

    protected EntityOpHandler newEntityOpHandler() {
        return new BatchEntityOpHandler(this);
    }

    protected abstract DefaultRepository doNewRepository(EntityElement entityElement);

}
