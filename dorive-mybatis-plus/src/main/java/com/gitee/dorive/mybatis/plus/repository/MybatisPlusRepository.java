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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.api.ImplFactory;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.option.QueryStrategy;
import com.gitee.dorive.mybatis.plus.impl.MybatisPlusExecutor;
import com.gitee.dorive.query.api.QueryBuilder;
import com.gitee.dorive.query.entity.BuildQuery;
import com.gitee.dorive.ref.repository.AbstractRefRepository;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.impl.CountQuerier;
import com.gitee.dorive.sql.impl.SegmentBuilder;
import com.gitee.dorive.sql.impl.SqlQueryBuilder;
import com.gitee.dorive.sql.impl.UnionExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> {

    private BaseMapper<Object> mapper;
    private EntityStoreInfo entityStoreInfo;
    private SqlRunner sqlRunner;
    private QueryBuilder sqlQueryBuilder;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        SegmentBuilder segmentBuilder = new SegmentBuilder();
        this.sqlQueryBuilder = new SqlQueryBuilder(segmentBuilder, sqlRunner);
        this.countQuerier = new CountQuerier(this, segmentBuilder, sqlRunner);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EntityStoreInfo resolveEntityStoreInfo(EntityDef entityDef, EntityEle entityEle) {
        Class<?> mapperClass = entityDef.getSource();
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = (BaseMapper<Object>) getApplicationContext().getBean(mapperClass);
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

        String tableName = tableInfo.getTableName();
        Map<String, String> propAliasMapping = getPropAliasMapping(tableInfo);
        String selectColumns = getSelectColumns(propAliasMapping);
        entityStoreInfo = new EntityStoreInfo(pojoClass, tableName, propAliasMapping, selectColumns);
        return entityStoreInfo;
    }

    private Map<String, String> getPropAliasMapping(TableInfo tableInfo) {
        String keyProperty = tableInfo.getKeyProperty();
        String keyColumn = tableInfo.getKeyColumn();
        List<TableFieldInfo> tableFieldInfos = tableInfo.getFieldList();

        Map<String, String> propAliasMapping = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(keyProperty) && StringUtils.isNotBlank(keyColumn)) {
            propAliasMapping.put(keyProperty, keyColumn);
        }
        for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
            propAliasMapping.put(tableFieldInfo.getProperty(), tableFieldInfo.getColumn());
        }
        return propAliasMapping;
    }

    private String getSelectColumns(Map<String, String> propAliasMapping) {
        List<String> selectColumns = new ArrayList<>(propAliasMapping.values());
        return StrUtil.join(",", selectColumns);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(EntityDef entityDef, EntityEle entityEle) {
        Executor executor = new MybatisPlusExecutor(entityDef, entityEle, mapper, (Class<Object>) entityStoreInfo.getPojoClass());
        ImplFactory implFactory = getApplicationContext().getBean(ImplFactory.class);
        sqlRunner = implFactory.getInstance(SqlRunner.class);
        return new UnionExecutor(executor, entityStoreInfo, sqlRunner);
    }

    @Override
    protected QueryBuilder adaptiveQueryBuilder(Context context, BuildQuery buildQuery) {
        QueryStrategy queryStrategy = (QueryStrategy) context.getOption(QueryStrategy.class);
        if (queryStrategy == null || queryStrategy == QueryStrategy.SQL) {
            return sqlQueryBuilder;
        }
        return super.adaptiveQueryBuilder(context, buildQuery);
    }

}
