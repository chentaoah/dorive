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

package com.gitee.dorive.mybatis.plus.impl.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.core.api.common.ImplFactory;
import com.gitee.dorive.core.api.common.MethodInvoker;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.executor.ExampleExecutor;
import com.gitee.dorive.core.impl.executor.FactoryExecutor;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.factory.ValueObjEntityFactory;
import com.gitee.dorive.core.impl.repository.AbstractRepository;
import com.gitee.dorive.core.impl.repository.DefaultRepository;
import com.gitee.dorive.core.impl.resolver.EntityMapperResolver;
import com.gitee.dorive.mybatis.plus.impl.executor.MybatisPlusExecutor;
import com.gitee.dorive.mybatis.plus.impl.DefaultMethodInvoker;
import com.gitee.dorive.sql.impl.handler.SqlCustomQueryHandler;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.enums.QueryMethod;
import com.gitee.dorive.ref.impl.repository.AbstractRefRepository;
import com.gitee.dorive.sql.api.CountQuerier;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.common.CountQuery;
import com.gitee.dorive.sql.impl.executor.UnionExecutor;
import com.gitee.dorive.sql.impl.handler.SqlBuildQueryHandler;
import com.gitee.dorive.sql.impl.handler.SqlExecuteQueryHandler;
import com.gitee.dorive.sql.impl.querier.SqlCountQuerier;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> implements CountQuerier {

    private SqlRunner sqlRunner;
    private EntityStoreInfo entityStoreInfo;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        ImplFactory implFactory = getApplicationContext().getBean(ImplFactory.class);
        this.sqlRunner = implFactory.getInstance(SqlRunner.class);
        super.afterPropertiesSet();
        this.countQuerier = new SqlCountQuerier(this, getQueryHandler(), this.sqlRunner);
    }

    @Override
    protected AbstractRepository<Object, Object> doNewRepository(EntityElement entityElement, OperationFactory operationFactory) {
        Map<String, Object> attributes = new ConcurrentHashMap<>(4);

        EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(getRepositoryDef());
        attributes.put(EntityStoreInfo.class.getName(), entityStoreInfo);

        EntityMapperResolver entityMapperResolver = new EntityMapperResolver(entityElement, entityStoreInfo);
        EntityMapper entityMapper = entityMapperResolver.newEntityMapper();
        EntityFactory entityFactory = newEntityFactory(entityElement, entityStoreInfo, entityMapper);

        Executor executor = newExecutor(entityElement, entityStoreInfo);
        executor = new FactoryExecutor(executor, entityElement, entityStoreInfo, entityFactory);
        executor = new ExampleExecutor(executor, entityElement, entityMapper);
        attributes.put(ExampleExecutor.class.getName(), executor);

        DefaultRepository defaultRepository = new DefaultRepository();
        defaultRepository.setEntityElement(entityElement);
        defaultRepository.setOperationFactory(operationFactory);
        defaultRepository.setExecutor(executor);
        defaultRepository.setAttributes(attributes);
        return defaultRepository;
    }

    protected EntityFactory newEntityFactory(EntityElement entityElement, EntityStoreInfo entityStoreInfo, EntityMapper entityMapper) {
        RepositoryDef repositoryDef = getRepositoryDef();
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            List<FieldConverter> valueObjFields = entityMapper.getValueObjFields();
            entityFactory = valueObjFields.isEmpty() ? new DefaultEntityFactory() : new ValueObjEntityFactory();
        } else {
            entityFactory = (EntityFactory) getApplicationContext().getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityElement(entityElement);
            defaultEntityFactory.setEntityStoreInfo(entityStoreInfo);
            defaultEntityFactory.setEntityMapper(entityMapper);
            defaultEntityFactory.setBoundedContextName(repositoryDef.getBoundedContext());
            defaultEntityFactory.setBoundedContext(getBoundedContext());
        }
        return entityFactory;
    }

    protected EntityStoreInfo resolveEntityStoreInfo(RepositoryDef repositoryDef) {
        Class<?> mapperClass = repositoryDef.getDataSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = getApplicationContext().getBean(mapperClass);
            Assert.notNull(mapper, "The mapper cannot be null! data source: {}", mapperClass);
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                Type genericInterface = mapperClass.getGenericInterfaces()[0];
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    pojoClass = (Class<?>) actualTypeArgument;
                }
            }
        }

        Assert.notNull(pojoClass, "The class of pojo cannot be null! data source: {}", mapperClass);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(pojoClass);
        Assert.notNull(tableInfo, "The table info cannot be null! data source: {}", mapperClass);
        assert tableInfo != null;
        this.entityStoreInfo = newEntityStoreInfo(mapperClass, mapper, pojoClass, tableInfo);
        return entityStoreInfo;
    }

    protected EntityStoreInfo newEntityStoreInfo(Class<?> mapperClass, Object mapper, Class<?> pojoClass, TableInfo tableInfo) {
        String tableName = tableInfo.getTableName();
        String keyProperty = tableInfo.getKeyProperty();
        String keyColumn = tableInfo.getKeyColumn();
        List<TableFieldInfo> tableFieldInfos = tableInfo.getFieldList();
        int size = tableFieldInfos.size() + 1;

        Map<String, String> propAliasMappingWithoutPk = new LinkedHashMap<>(size * 4 / 3 + 1);
        for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
            propAliasMappingWithoutPk.put(tableFieldInfo.getProperty(), tableFieldInfo.getColumn());
        }

        Map<String, String> propAliasMapping = new LinkedHashMap<>(size * 4 / 3 + 1);
        if (StringUtils.isNotBlank(keyProperty) && StringUtils.isNotBlank(keyColumn)) {
            propAliasMapping.put(keyProperty, keyColumn);
        }
        propAliasMapping.putAll(propAliasMappingWithoutPk);

        Map<String, String> aliasPropMapping = MapUtil.reverse(propAliasMapping);

        List<String> columns = new ArrayList<>(propAliasMapping.values());
        String selectColumns = StrUtil.join(",", columns);

        Map<String, MethodInvoker> selectMethodMap = new ConcurrentHashMap<>(8);
        for (Method method : ReflectUtil.getMethodsDirectly(mapperClass, false, false)) {
            String name = method.getName();
            if (name.startsWith("select") || name.startsWith("query")) {
                MethodInvoker methodInvoker = new DefaultMethodInvoker(mapper, method);
                selectMethodMap.putIfAbsent(name, methodInvoker);
            }
        }
        return new EntityStoreInfo(mapperClass, mapper, pojoClass, tableName, keyProperty, keyColumn, propAliasMappingWithoutPk, propAliasMapping, aliasPropMapping, selectColumns, selectMethodMap);
    }

    protected Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo) {
        Executor executor = new MybatisPlusExecutor(entityElement.getEntityDef(), entityElement, entityStoreInfo);
        return new UnionExecutor(executor, sqlRunner, entityStoreInfo);
    }

    @Override
    protected void registryQueryHandlers(Map<QueryMethod, QueryHandler> queryHandlerMap) {
        super.registryQueryHandlers(queryHandlerMap);
        queryHandlerMap.put(QueryMethod.SQL_BUILD, new SqlBuildQueryHandler(this));
        queryHandlerMap.put(QueryMethod.SQL_EXECUTE, new SqlExecuteQueryHandler(this, sqlRunner));
        queryHandlerMap.put(QueryMethod.SQL_CUSTOM, new SqlCustomQueryHandler(this, entityStoreInfo));
    }

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        return countQuerier.selectCountMap(context, countQuery);
    }

}
