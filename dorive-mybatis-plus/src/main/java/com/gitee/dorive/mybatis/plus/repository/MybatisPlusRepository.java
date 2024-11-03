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

package com.gitee.dorive.mybatis.plus.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.def.EntityDef;
import com.gitee.dorive.core.api.common.ImplFactory;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.mybatis.plus.executor.MybatisPlusExecutor;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.enums.QueryMethod;
import com.gitee.dorive.ref.repository.AbstractRefRepository;
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> implements CountQuerier {

    private SqlRunner sqlRunner;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        ImplFactory implFactory = getApplicationContext().getBean(ImplFactory.class);
        this.sqlRunner = implFactory.getInstance(SqlRunner.class);
        super.afterPropertiesSet();
        this.countQuerier = new SqlCountQuerier(this, getQueryHandler(), this.sqlRunner);
    }

    @Override
    protected EntityStoreInfo resolveEntityStoreInfo(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> mapperClass = entityDef.getSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = getApplicationContext().getBean(mapperClass);
            Assert.notNull(mapper, "The mapper cannot be null! source: {}", mapperClass);
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

        Assert.notNull(pojoClass, "The class of pojo cannot be null! source: {}", mapperClass);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(pojoClass);
        Assert.notNull(tableInfo, "The table info cannot be null! source: {}", mapperClass);
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

        return new EntityStoreInfo(mapperClass, mapper, pojoClass, tableName, keyProperty, keyColumn, propAliasMappingWithoutPk, propAliasMapping, aliasPropMapping, selectColumns);
    }

    @Override
    protected Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo) {
        Executor executor = new MybatisPlusExecutor(entityElement.getEntityDef(), entityElement, entityStoreInfo);
        return new UnionExecutor(executor, sqlRunner, entityStoreInfo);
    }

    @Override
    protected void registryQueryHandlers(Map<QueryMethod, QueryHandler> queryHandlerMap) {
        super.registryQueryHandlers(queryHandlerMap);
        queryHandlerMap.put(QueryMethod.SQL_BUILD, new SqlBuildQueryHandler(this, null));
        queryHandlerMap.put(QueryMethod.SQL_EXECUTE, new SqlExecuteQueryHandler(this, null, sqlRunner));
    }

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        return countQuerier.selectCountMap(context, countQuery);
    }

}
