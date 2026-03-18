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
import com.gitee.dorive.base.v1.factory.api.Transformer;
import com.gitee.dorive.base.v1.factory.api.TransformerManager;
import com.gitee.dorive.base.v1.factory.enums.Category;
import com.gitee.dorive.base.v1.mybatis.api.MethodInvoker;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.factory.v1.api.EntityFactory;
import com.gitee.dorive.factory.v1.api.EntityTransformer;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.factory.v1.impl.executor.ExampleExecutor;
import com.gitee.dorive.factory.v1.impl.executor.FactoryExecutor;
import com.gitee.dorive.factory.v1.impl.resolver.EntityTransformerManagerResolver;
import com.gitee.dorive.factory.v1.impl.resolver.EntityFactoryResolver;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.executor.v1.impl.executor.UnionExecutor;
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

        // 存储信息
        EntityStoreInfo entityStoreInfo = resolveEntityStoreInfo(repository.getRepositoryDef());

        // 别名转换
        String reCategory = Category.ENTITY_DATABASE.name();
        String deCategory = Category.ENTITY_POJO.name();
        EntityTransformerManagerResolver entityTransformerManagerResolver = new EntityTransformerManagerResolver(entityElement, entityStoreInfo.getAliasPropMap(), reCategory, deCategory);
        EntityTransformerManager entityTransformerManager = entityTransformerManagerResolver.newEntityTransformerManager();
        EntityTransformer reEntityTransformer = (EntityTransformer) entityTransformerManager.getTransformer(reCategory);
        EntityTransformer deEntityTransformer = (EntityTransformer) entityTransformerManager.getTransformer(deCategory);

        // 实体工厂
        EntityFactoryResolver entityFactoryResolver = new EntityFactoryResolver(
                repository, entityElement, entityElement.getGenericType(), entityStoreInfo.getPojoClass(),
                entityTransformerManager, reEntityTransformer, deEntityTransformer);
        EntityFactory entityFactory = entityFactoryResolver.newEntityFactory();

        // 执行器
        Executor executor = newExecutor(entityElement, entityStoreInfo);
        executor = new UnionExecutor(executor, repository.getSqlRunner(), entityStoreInfo);
        executor = new FactoryExecutor(executor, entityElement, entityStoreInfo.getIdProperty(), entityFactory);
        executor = new ExampleExecutor(executor, entityElement, reEntityTransformer);

        // 查询条件转换器
        ExampleConverter exampleConverter = (ExampleConverter) executor;

        repository.setProperty(EntityStoreInfo.class, entityStoreInfo);
        repository.setProperty(EntityTransformerManager.class, entityTransformerManager);
        repository.setProperty(TransformerManager.class, entityTransformerManager);
        repository.setProperty(Transformer.class, reEntityTransformer);
        repository.setProperty(ExampleConverter.class, exampleConverter);

        DefaultRepository defaultRepository = new DefaultRepository();
        defaultRepository.setEntityElement(entityElement);
        defaultRepository.setOperationFactory(operationFactory);
        defaultRepository.setExecutor(executor);
        defaultRepository.setProperty(EntityStoreInfo.class, entityStoreInfo);
        defaultRepository.setProperty(EntityTransformerManager.class, entityTransformerManager);
        defaultRepository.setProperty(TransformerManager.class, entityTransformerManager);
        defaultRepository.setProperty(Transformer.class, reEntityTransformer);
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

        Map<String, String> propAliasMapWithoutPk = new LinkedHashMap<>(size * 4 / 3 + 1);
        for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
            propAliasMapWithoutPk.put(tableFieldInfo.getProperty(), tableFieldInfo.getColumn());
        }

        Map<String, String> propAliasMap = new LinkedHashMap<>(size * 4 / 3 + 1);
        if (StringUtils.isNotBlank(keyProperty) && StringUtils.isNotBlank(keyColumn)) {
            propAliasMap.put(keyProperty, keyColumn);
        }
        propAliasMap.putAll(propAliasMapWithoutPk);

        Map<String, String> aliasPropMap = MapUtil.reverse(propAliasMap);

        List<String> columns = new ArrayList<>(propAliasMap.values());
        String selectColumns = StrUtil.join(",", columns);

        Map<String, MethodInvoker> selectMethodMap = new ConcurrentHashMap<>(8);
        for (Method method : ReflectUtil.getMethodsDirectly(mapperClass, false, false)) {
            String name = method.getName();
            if (name.startsWith("select") || name.startsWith("query")) {
                MethodInvoker methodInvoker = new DefaultMethodInvoker(mapper, method);
                selectMethodMap.putIfAbsent(name, methodInvoker);
            }
        }
        return new EntityStoreInfo(mapperClass, mapper, pojoClass, tableName, keyProperty, keyColumn, propAliasMapWithoutPk, propAliasMap, aliasPropMap, selectColumns, selectMethodMap);
    }

    private Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo) {
        return new MybatisPlusExecutor(entityElement.getEntityDef(), entityElement, entityStoreInfo);
    }

}
