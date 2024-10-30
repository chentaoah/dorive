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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.core.Order;
import com.gitee.dorive.api.entity.core.EntityDefinition;
import com.gitee.dorive.api.entity.core.def.EntityDef;
import com.gitee.dorive.api.entity.core.def.OrderDef;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.impl.core.EntityDefinitionResolver;
import com.gitee.dorive.api.impl.core.EntityElementResolver;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.EntityOpHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.config.RepositoryContext;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.context.SelectTypeMatcher;
import com.gitee.dorive.core.impl.executor.ContextExecutor;
import com.gitee.dorive.core.impl.executor.ExampleExecutor;
import com.gitee.dorive.core.impl.executor.FactoryExecutor;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.factory.ValueObjEntityFactory;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.handler.eo.BatchEntityOpHandler;
import com.gitee.dorive.core.impl.handler.DelegatedEntityHandler;
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

@Getter
@Setter
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
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
    public void afterPropertiesSet() throws Exception {
        Class<?> entityClass = ReflectUtils.getFirstArgumentType(this.getClass());

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

        EntityHandler entityHandler = new BatchEntityHandler(this);
        entityHandler = processEntityHandler(entityHandler);
        EntityOpHandler entityOpHandler = new BatchEntityOpHandler(this);
        derivedRepositoryResolver = new DerivedRepositoryResolver(this);
        derivedRepositoryResolver.resolve();
        if (derivedRepositoryResolver.hasDerived()) {
            entityHandler = new DelegatedEntityHandler(this, derivedRepositoryResolver.getEntityHandlerMap(entityHandler));
            entityOpHandler = new DelegatedEntityOpHandler(this, derivedRepositoryResolver.getEntityOpHandlerMap(entityOpHandler));
        }
        Executor executor = new ContextExecutor(this, entityHandler, entityOpHandler);
        setExecutor(executor);
    }

    private CommonRepository newRepository(EntityElement entityElement) {
        resetEntityDef(entityElement);

        String accessPath = entityElement.getAccessPath();
        EntityDef entityDef = entityElement.getEntityDef();
        OrderDef orderDef = entityElement.getOrderDef();

        OperationFactory operationFactory = new OperationFactory(entityElement);

        AbstractRepository<Object, Object> actualRepository = doNewRepository(entityElement, operationFactory);
        AbstractRepository<Object, Object> proxyRepository = processRepository(actualRepository);

        boolean isRoot = "/".equals(accessPath);
        boolean isAggregated = entityDef.getRepository() != Object.class;
        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolve(entityElement);
        OrderBy defaultOrderBy = newDefaultOrderBy(orderDef);

        CommonRepository repository = new CommonRepository();
        repository.setEntityElement(entityElement);
        repository.setOperationFactory(operationFactory);
        repository.setProxyRepository(proxyRepository);
        repository.setAccessPath(accessPath);
        repository.setRoot(isRoot);
        repository.setAggregated(isAggregated);
        repository.setBinderResolver(binderResolver);
        repository.setDefaultOrderBy(defaultOrderBy);
        repository.setBound(false);
        repository.setMatcher(new SelectTypeMatcher(repository));
        return repository;
    }

    private void resetEntityDef(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        if (entityDef.isAggregate()) {
            Class<?> entityClass = entityElement.getGenericType();
            Class<?> repositoryClass = RepositoryContext.findRepositoryClass(entityClass);
            Assert.notNull(repositoryClass, "No type of repository found! type: {}", entityClass.getName());
            entityDef.setRepository(repositoryClass);
        }
    }

    @SuppressWarnings("unchecked")
    private AbstractRepository<Object, Object> doNewRepository(EntityElement entityElement, OperationFactory operationFactory) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> repositoryClass = entityDef.getRepository();
        Object repository;
        if (repositoryClass == Object.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityElement(entityElement);
            defaultRepository.setOperationFactory(operationFactory);

            Map<String, Object> attributes = entityElement.getAttributes();

            EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(entityElement);
            attributes.put(EntityStoreInfo.class.getName(), entityStoreInfo);

            EntityMapperResolver entityMapperResolver = new EntityMapperResolver(entityElement, entityStoreInfo);
            EntityMapper entityMapper = entityMapperResolver.newEntityMapper();
            EntityFactory entityFactory = newEntityFactory(entityElement, entityStoreInfo, entityMapper);

            Executor executor = newExecutor(entityElement, entityStoreInfo);
            executor = new FactoryExecutor(executor, entityElement, entityStoreInfo, entityFactory);
            executor = new ExampleExecutor(executor, entityElement, entityMapper);
            attributes.put(ExampleExecutor.class.getName(), executor);
            defaultRepository.setExecutor(executor);
        }
        return (AbstractRepository<Object, Object>) repository;
    }

    private EntityFactory newEntityFactory(EntityElement entityElement, EntityStoreInfo entityStoreInfo, EntityMapper entityMapper) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> factoryClass = entityDef.getFactory();
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
        }
        return entityFactory;
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

    protected abstract EntityStoreInfo resolveEntityStoreInfo(EntityElement entityElement);

    protected abstract Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo);

    protected abstract AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository);

    protected abstract EntityHandler processEntityHandler(EntityHandler entityHandler);

}
