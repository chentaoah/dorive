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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.api.def.EntityDef;
import com.gitee.dorive.api.def.OrderDef;
import com.gitee.dorive.api.ele.EntityElement;
import com.gitee.dorive.api.entity.EntityDefinition;
import com.gitee.dorive.api.impl.EntityDefinitionReader;
import com.gitee.dorive.api.impl.EntityElementResolver;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.config.RepositoryContext;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.context.SelectTypeMatcher;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.impl.executor.ContextExecutor;
import com.gitee.dorive.core.impl.executor.ExampleExecutor;
import com.gitee.dorive.core.impl.executor.FactoryExecutor;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.factory.ValueObjEntityFactory;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DerivedResolver;
import com.gitee.dorive.core.impl.resolver.EntityMapperResolver;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    private static final Map<EntityElement, EntityStoreInfo> ENTITY_STORE_INFO_MAP = new ConcurrentHashMap<>();
    private static final Map<EntityElement, ExampleExecutor> EXAMPLE_EXECUTOR_MAP = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    private DerivedResolver derivedResolver;

    private Map<String, CommonRepository> repositoryMap = new LinkedHashMap<>();
    private CommonRepository rootRepository;
    private List<CommonRepository> subRepositories = new ArrayList<>();
    private List<CommonRepository> orderedRepositories = new ArrayList<>();

    public static EntityStoreInfo getEntityStoreInfo(EntityElement entityElement) {
        return ENTITY_STORE_INFO_MAP.get(entityElement);
    }

    public static ExampleExecutor getExampleExecutor(EntityElement entityElement) {
        return EXAMPLE_EXECUTOR_MAP.get(entityElement);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class<?> entityClass = ReflectUtils.getFirstArgumentType(this.getClass());

        EntityDefinitionReader entityDefinitionReader = new EntityDefinitionReader();
        EntityDefinition entityDefinition = entityDefinitionReader.read(entityClass);

        EntityElementResolver entityElementResolver = new EntityElementResolver();
        Map<String, EntityElement> entityElementMap = entityElementResolver.resolve(entityDefinition);

        entityElementMap.forEach((accessPath, entityElement) -> {
            CommonRepository repository = newRepository(accessPath, entityElement);
            repositoryMap.put(accessPath, repository);
            if ("/".equals(accessPath)) {
                rootRepository = repository;
            } else {
                subRepositories.add(repository);
            }
            orderedRepositories.add(repository);
        });

        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityDef().getPriority()));

        setEntityDef(rootRepository.getEntityDef());
        setOrderDef(rootRepository.getOrderDef());
        setEntityElement(rootRepository.getEntityElement());
        setOperationFactory(rootRepository.getOperationFactory());

        derivedResolver = new DerivedResolver(this);

        EntityHandler entityHandler = processEntityHandler(new BatchEntityHandler(this));
        Executor executor = new ContextExecutor(this, entityHandler);
        setExecutor(executor);
    }

    private CommonRepository newRepository(String accessPath, EntityElement entityElement) {
        EntityDef entityDef = renewEntityDef(entityElement);
        OrderDef orderDef = renewOrderDef(entityElement);
        OperationFactory operationFactory = new OperationFactory(entityElement);

        AbstractRepository<Object, Object> actualRepository = doNewRepository(entityDef, entityElement, operationFactory);
        AbstractRepository<Object, Object> proxyRepository = processRepository(actualRepository);

        boolean isRoot = "/".equals(accessPath);
        boolean isAggregated = entityDef.getRepository() != Object.class;
        OrderBy defaultOrderBy = newDefaultOrderBy(orderDef);

        BinderResolver binderResolver = new BinderResolver(this, entityElement);
        binderResolver.resolve(accessPath, entityElement);

        CommonRepository repository = new CommonRepository();
        repository.setEntityDef(entityDef);
        repository.setOrderDef(orderDef);
        repository.setEntityElement(entityElement);
        repository.setOperationFactory(operationFactory);
        repository.setProxyRepository(proxyRepository);

        repository.setAccessPath(accessPath);
        repository.setRoot(isRoot);
        repository.setAggregated(isAggregated);
        repository.setDefaultOrderBy(defaultOrderBy);

        repository.setBinderResolver(binderResolver);
        repository.setBoundEntity(false);
        repository.setMatcher(new SelectTypeMatcher(repository));
        return repository;
    }

    private EntityDef renewEntityDef(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        entityDef = BeanUtil.copyProperties(entityDef, EntityDef.class);
        if (entityDef.isAggregate()) {
            Class<?> entityClass = entityElement.getGenericType();
            Class<?> repositoryClass = RepositoryContext.findRepositoryClass(entityClass);
            Assert.notNull(repositoryClass, "No type of repository found! type: {}", entityClass.getName());
            entityDef.setRepository(repositoryClass);
        }
        return entityDef;
    }

    private OrderDef renewOrderDef(EntityElement entityElement) {
        OrderDef orderDef = entityElement.getOrderDef();
        return orderDef == null ? new OrderDef("", "") : BeanUtil.copyProperties(orderDef, OrderDef.class);
    }

    @SuppressWarnings("unchecked")
    private AbstractRepository<Object, Object> doNewRepository(EntityDef entityDef, EntityElement entityElement, OperationFactory operationFactory) {
        Class<?> repositoryClass = entityDef.getRepository();
        Object repository;
        if (repositoryClass == Object.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityDef(entityDef);
            defaultRepository.setEntityElement(entityElement);
            defaultRepository.setOperationFactory(operationFactory);

            EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(entityDef, entityElement);
            ENTITY_STORE_INFO_MAP.put(entityElement, entityStoreInfo);

            EntityMapperResolver entityMapperResolver = new EntityMapperResolver(entityElement, entityStoreInfo);
            EntityMapper entityMapper = entityMapperResolver.newEntityMapper();
            EntityFactory entityFactory = newEntityFactory(entityDef, entityElement, entityStoreInfo, entityMapper);

            Executor executor = newExecutor(entityDef, entityElement, entityStoreInfo);
            executor = new FactoryExecutor(executor, entityElement, entityStoreInfo, entityFactory);
            executor = new ExampleExecutor(executor, entityElement, entityMapper);
            EXAMPLE_EXECUTOR_MAP.put(entityElement, (ExampleExecutor) executor);
            defaultRepository.setExecutor(executor);
        }
        return (AbstractRepository<Object, Object>) repository;
    }

    private EntityFactory newEntityFactory(EntityDef entityDef, EntityElement entityElement, EntityStoreInfo entityStoreInfo, EntityMapper entityMapper) {
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
        String sortBy = orderDef.getSortBy();
        String order = orderDef.getOrder().toUpperCase();
        if (StringUtils.isNotBlank(sortBy) && (Order.ASC.equals(order) || Order.DESC.equals(order))) {
            List<String> properties = StrUtil.splitTrim(sortBy, ",");
            return new OrderBy(properties, order);
        }
        return null;
    }

    protected abstract EntityStoreInfo resolveEntityStoreInfo(EntityDef entityDef, EntityElement entityElement);

    protected abstract Executor newExecutor(EntityDef entityDef, EntityElement entityElement, EntityStoreInfo entityStoreInfo);

    protected abstract AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository);

    protected abstract EntityHandler processEntityHandler(EntityHandler entityHandler);

}
