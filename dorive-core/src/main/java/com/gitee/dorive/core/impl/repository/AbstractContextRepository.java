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
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.BoundedContextAware;
import com.gitee.dorive.api.constant.core.Order;
import com.gitee.dorive.api.entity.core.EntityDefinition;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.def.EntityDef;
import com.gitee.dorive.api.entity.core.def.OrderDef;
import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.api.impl.core.EntityDefinitionResolver;
import com.gitee.dorive.api.impl.core.EntityElementResolver;
import com.gitee.dorive.api.impl.util.ReflectUtils;
import com.gitee.dorive.core.api.common.RepositoryPostProcessor;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.EntityOpHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.config.RepositoryContext;
import com.gitee.dorive.api.entity.BoundedContext;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.context.AdaptiveMatcher;
import com.gitee.dorive.core.impl.executor.ContextExecutor;
import com.gitee.dorive.core.impl.executor.ExampleExecutor;
import com.gitee.dorive.core.impl.executor.FactoryExecutor;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.factory.ValueObjEntityFactory;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.handler.DelegatedEntityHandler;
import com.gitee.dorive.core.impl.handler.eo.BatchEntityOpHandler;
import com.gitee.dorive.core.impl.handler.eo.DelegatedEntityOpHandler;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DerivedRepositoryResolver;
import com.gitee.dorive.core.impl.resolver.EntityMapperResolver;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, BoundedContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private BoundedContext boundedContext;

    private RepositoryDef repositoryDef;
    private Map<String, CommonRepository> repositoryMap = new LinkedHashMap<>();
    private CommonRepository rootRepository;
    private List<CommonRepository> subRepositories = new ArrayList<>();
    private List<CommonRepository> orderedRepositories = new ArrayList<>();
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
            CommonRepository repository = newRepository(entityElement);
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

    private CommonRepository newRepository(EntityElement entityElement) {
        resetEntityDef(entityElement);

        EntityDef entityDef = entityElement.getEntityDef();
        OrderDef orderDef = entityElement.getOrderDef();
        String accessPath = entityElement.getAccessPath();

        OperationFactory operationFactory = new OperationFactory(entityElement);

        Class<?> repositoryClass = entityDef.getRepository();
        AbstractRepository<Object, Object> repository;
        if (repositoryClass == DefaultRepository.class) {
            repository = doNewRepository(entityElement, operationFactory);
        } else {
            repository = doGetRepository(entityElement);
        }

        boolean isRoot = "/".equals(accessPath);
        boolean isAggregated = repository instanceof AbstractContextRepository;
        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolve(entityElement);
        OrderBy defaultOrderBy = newDefaultOrderBy(orderDef);

        CommonRepository repositoryWrapper = new CommonRepository();
        repositoryWrapper.setEntityElement(entityElement);
        repositoryWrapper.setOperationFactory(operationFactory);
        repositoryWrapper.setProxyRepository(repository);
        repositoryWrapper.setAccessPath(accessPath);
        repositoryWrapper.setRoot(isRoot);
        repositoryWrapper.setAggregated(isAggregated);
        repositoryWrapper.setBinderResolver(binderResolver);
        repositoryWrapper.setDefaultOrderBy(defaultOrderBy);
        repositoryWrapper.setBound(false);
        repositoryWrapper.setMatcher(new AdaptiveMatcher(repositoryWrapper));
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

    protected AbstractRepository<Object, Object> doNewRepository(EntityElement entityElement, OperationFactory operationFactory) {
        Map<String, Object> attributes = new ConcurrentHashMap<>(4);

        EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(repositoryDef);
        attributes.put(EntityStoreInfo.class.getName(), entityStoreInfo);

        EntityMapperResolver entityMapperResolver = new EntityMapperResolver(entityElement, entityStoreInfo);
        EntityMapper entityMapper = entityMapperResolver.newEntityMapper();
        EntityFactory entityFactory = newEntityFactory(entityElement, entityStoreInfo, entityMapper);

        Executor executor = newExecutor(entityElement, entityStoreInfo);
        executor = new FactoryExecutor(executor, entityElement, entityStoreInfo, entityFactory);
        executor = new ExampleExecutor(executor, entityElement, entityMapper);
        attributes.put(ExampleExecutor.class.getName(), executor);

        DefaultRepository repository = new DefaultRepository();
        repository.setEntityElement(entityElement);
        repository.setOperationFactory(operationFactory);
        repository.setExecutor(executor);
        repository.setAttributes(attributes);
        return repository;
    }

    private EntityFactory newEntityFactory(EntityElement entityElement, EntityStoreInfo entityStoreInfo, EntityMapper entityMapper) {
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            List<FieldConverter> valueObjFields = entityMapper.getValueObjFields();
            entityFactory = valueObjFields.isEmpty() ? new DefaultEntityFactory() : new ValueObjEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityElement(entityElement);
            defaultEntityFactory.setEntityStoreInfo(entityStoreInfo);
            defaultEntityFactory.setEntityMapper(entityMapper);
            defaultEntityFactory.setBoundedContextName(repositoryDef.getBoundedContext());
            defaultEntityFactory.setBoundedContext(boundedContext);
        }
        return entityFactory;
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

    private OrderBy newDefaultOrderBy(OrderDef orderDef) {
        if (orderDef != null) {
            String sortBy = orderDef.getSortBy();
            String order = orderDef.getOrder();
            if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(order)) {
                order = order.toUpperCase();
                if (Order.ASC.equals(order) || Order.DESC.equals(order)) {
                    List<String> properties = StrUtil.splitTrim(sortBy, ",");
                    return new OrderBy(properties, order);
                }
            }
        }
        return null;
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

    protected abstract EntityStoreInfo resolveEntityStoreInfo(RepositoryDef repositoryDef);

    protected abstract Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo);

}
