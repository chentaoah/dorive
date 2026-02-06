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

package com.gitee.dorive.launcher.v1.impl.factory;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.factory.api.ExampleConverter;
import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.base.v1.factory.api.TranslatorManager;
import com.gitee.dorive.base.v1.factory.enums.Category;
import com.gitee.dorive.base.v1.mybatis.api.MethodInvoker;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.factory.v1.api.EntityFactory;
import com.gitee.dorive.factory.v1.api.EntityMapper;
import com.gitee.dorive.factory.v1.api.EntityMappers;
import com.gitee.dorive.factory.v1.impl.executor.ExampleExecutor;
import com.gitee.dorive.factory.v1.impl.executor.FactoryExecutor;
import com.gitee.dorive.factory.v1.impl.resolver.EntityMappersResolver;
import com.gitee.dorive.launcher.v1.impl.resolver.EntityFactoryResolver;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.binder.v1.impl.union.UnionExecutor;
import com.gitee.dorive.mybatis.plus.v1.impl.common.DefaultMethodInvoker;
import com.gitee.dorive.mybatis.plus.v1.impl.executor.MybatisPlusExecutor;
import com.gitee.dorive.repository.v1.impl.repository.MybatisPlusRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class MybatisPlusRepositoryBuilder {

    private final MybatisPlusRepository<?, ?> repository;

    public AbstractRepository<Object, Object> newRepository(EntityElement entityElement) {
        OperationFactory operationFactory = new OperationFactory(entityElement);

        EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(repository.getRepositoryDef());
        repository.setProperty(EntityStoreInfo.class, entityStoreInfo);

        String reMapper = Category.ENTITY_DATABASE.name();
        String deMapper = Category.ENTITY_POJO.name();

        EntityMappersResolver entityMappersResolver = new EntityMappersResolver(entityElement, entityStoreInfo.getAliasPropMapping(), reMapper, deMapper);
        EntityMappers entityMappers = entityMappersResolver.newEntityMappers();
        repository.setProperty(EntityMappers.class, entityMappers);

        // 命名转换器管理
        TranslatorManager translatorManager = entityMappers::getEntityMapper;
        Translator translator = translatorManager.getTranslator(Category.ENTITY_DATABASE.name());
        repository.setProperty(TranslatorManager.class, translatorManager);
        repository.setProperty(Translator.class, translator);

        EntityMapper reEntityMapper = entityMappers.getEntityMapper(reMapper);
        EntityMapper deEntityMapper = entityMappers.getEntityMapper(deMapper);

        EntityFactoryResolver entityFactoryResolver = new EntityFactoryResolver(
                repository, entityElement, entityElement.getGenericType(), entityStoreInfo.getPojoClass(), entityMappers, reEntityMapper, deEntityMapper);
        EntityFactory entityFactory = entityFactoryResolver.newEntityFactory();

        Executor executor = newExecutor(entityElement, entityStoreInfo);
        executor = new UnionExecutor(executor, repository.getSqlRunner(), entityStoreInfo);
        executor = new FactoryExecutor(executor, entityElement, entityStoreInfo.getIdProperty(), entityFactory);
        executor = new ExampleExecutor(executor, entityElement, reEntityMapper);

        ExampleConverter exampleConverter = (ExampleConverter) executor;
        repository.setProperty(ExampleConverter.class, exampleConverter);

        DefaultRepository defaultRepository = new DefaultRepository();
        defaultRepository.setEntityElement(entityElement);
        defaultRepository.setOperationFactory(operationFactory);
        defaultRepository.setExecutor(executor);
        defaultRepository.setProperty(EntityStoreInfo.class, entityStoreInfo);
        defaultRepository.setProperty(EntityMappers.class, entityMappers);
        defaultRepository.setProperty(TranslatorManager.class, translatorManager);
        defaultRepository.setProperty(Translator.class, translator);
        defaultRepository.setProperty(ExampleConverter.class, exampleConverter);
        return defaultRepository;
    }

    private EntityStoreInfo resolveEntityStoreInfo(RepositoryDef repositoryDef) {
        Class<?> mapperClass = repositoryDef.getDataSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            ApplicationContext applicationContext = repository.getApplicationContext();
            mapper = applicationContext.getBean(mapperClass);
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
        return newEntityStoreInfo(mapperClass, mapper, pojoClass, tableInfo);
    }

    private EntityStoreInfo newEntityStoreInfo(Class<?> mapperClass, Object mapper, Class<?> pojoClass, TableInfo tableInfo) {
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

    private Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo) {
        return new MybatisPlusExecutor(entityElement.getEntityDef(), entityElement, entityStoreInfo);
    }

}
