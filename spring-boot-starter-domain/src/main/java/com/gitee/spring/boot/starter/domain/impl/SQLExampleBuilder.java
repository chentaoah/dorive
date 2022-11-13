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
package com.gitee.spring.boot.starter.domain.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.spring.boot.starter.domain.entity.JoinSegment;
import com.gitee.spring.boot.starter.domain.entity.Metadata;
import com.gitee.spring.boot.starter.domain.entity.SqlSegment;
import com.gitee.spring.domain.coating.api.ExampleBuilder;
import com.gitee.spring.domain.coating.entity.CoatingWrapper;
import com.gitee.spring.domain.coating.entity.PropertyWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.Property;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.entity.executor.Criterion;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.OrderBy;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.impl.binder.ContextBinder;
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.*;
import java.util.stream.Collectors;

public class SQLExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public SQLExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        CoatingWrapperResolver coatingWrapperResolver = repository.getCoatingWrapperResolver();
        Map<Class<?>, CoatingWrapper> coatingWrapperMap = coatingWrapperResolver.getCoatingWrapperMap();

        CoatingWrapper coatingWrapper = coatingWrapperMap.get(coatingObject.getClass());
        Assert.notNull(coatingWrapper, "No coating wrapper exists!");
        List<RepositoryWrapper> repositoryWrappers = coatingWrapper.getRepositoryWrappers();

        OrderBy orderByInfo = coatingWrapper.getOrderByInfo(coatingObject);
        Page<Object> pageInfo = coatingWrapper.getPageInfo(coatingObject);

        Map<String, SqlSegment> sqlSegmentMap = new LinkedHashMap<>(repositoryWrappers.size() * 4 / 3 + 1);
        SqlSegment rootSqlSegment = null;
        char letter = 'a';

        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

            BinderResolver binderResolver = definitionRepository.getBinderResolver();

            Example example = newExampleByCoating(repositoryWrapper, coatingObject);
            if (example.isDirtyQuery()) {
                appendCriteriaByContext(boundedContext, repositoryWrapper, example);
            }

            TableInfo tableInfo = getTableInfo(configuredRepository);
            String tableName = tableInfo.getTableName();

            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            List<JoinSegment> joinSegments = getJoinSegments(sqlSegmentMap, binderResolver, tableName, tableAlias);
            String sqlCriteria = example.isDirtyQuery() ? buildSqlCriteria(tableAlias, example) : null;

            if ("/".equals(absoluteAccessPath)) {
                String sql = String.format("SELECT %s.id FROM %s %s ", tableAlias, tableName, tableAlias);
                rootSqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, sqlCriteria, true, example.isDirtyQuery(), new HashSet<>(4));
                sqlSegmentMap.put(tableName, rootSqlSegment);

            } else {
                String sql = String.format("LEFT JOIN %s %s ON ", tableName, tableAlias);
                SqlSegment sqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, sqlCriteria, false, example.isDirtyQuery(), new HashSet<>(4));
                sqlSegmentMap.put(tableName, sqlSegment);
            }
        }

        Example example = new Example();
        example.setOrderBy(orderByInfo);
        example.setPage(pageInfo);

        assert rootSqlSegment != null;
        markReachableAndDirty(sqlSegmentMap, rootSqlSegment);
        if (!rootSqlSegment.isDirtyQuery()) {
            return example;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<String> sqlCriteria = new ArrayList<>(sqlSegmentMap.size());
        for (SqlSegment sqlSegment : sqlSegmentMap.values()) {
            if (sqlSegment.isRootReachable() && sqlSegment.isDirtyQuery()) {
                sqlBuilder.append(sqlSegment);

                List<JoinSegment> joinSegments = getAvailableJoinSegments(sqlSegmentMap, sqlSegment);
                if (!joinSegments.isEmpty()) {
                    sqlBuilder.append(StrUtil.join(" AND ", joinSegments)).append(" ");
                }

                if (sqlSegment.getSqlCriteria() != null) {
                    sqlCriteria.add(sqlSegment.getSqlCriteria());
                }
            }
        }
        sqlBuilder.append("WHERE ").append(StrUtil.join(" AND ", sqlCriteria));
        if (orderByInfo != null) {
            sqlBuilder.append(" ").append(orderByInfo);
        }
        if (pageInfo != null) {
            sqlBuilder.append(" ").append(pageInfo);
        }

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(sqlBuilder.toString());
        List<Object> primaryKeys = resultMaps.stream().map(map -> map.get("id")).filter(Objects::nonNull).collect(Collectors.toList());
        if (!primaryKeys.isEmpty()) {
            return example.in("id", primaryKeys);
        } else {
            example.setEmptyQuery(true);
        }

        return example;
    }

    private Example newExampleByCoating(RepositoryWrapper repositoryWrapper, Object coatingObject) {
        Example example = new Example();
        for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
            Property property = propertyWrapper.getProperty();
            Object fieldValue = property.getFieldValue(coatingObject);
            if (fieldValue != null) {
                PropertyDefinition propertyDefinition = propertyWrapper.getPropertyDefinition();
                String alias = propertyDefinition.getAlias();
                String operator = propertyDefinition.getOperator();
                example.addCriterion(new Criterion(alias, operator, fieldValue));
            }
        }
        return example;
    }

    private void appendCriteriaByContext(BoundedContext boundedContext, RepositoryWrapper repositoryWrapper, Example example) {
        RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        BinderResolver binderResolver = definitionRepository.getBinderResolver();
        for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
            Object boundValue = contextBinder.getBoundValue(boundedContext, null);
            if (boundValue != null) {
                BindingDefinition bindingDefinition = contextBinder.getBindingDefinition();
                String alias = bindingDefinition.getAlias();
                example.eq(alias, boundValue);
            }
        }
    }

    private TableInfo getTableInfo(ConfiguredRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private List<JoinSegment> getJoinSegments(Map<String, SqlSegment> sqlSegmentMap, BinderResolver binderResolver, String tableName, String tableAlias) {
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        List<JoinSegment> joinSegments = new ArrayList<>(propertyBinders.size());
        for (PropertyBinder propertyBinder : propertyBinders) {
            TableInfo joinTableInfo = getTableInfo(propertyBinder.getBelongRepository());
            String joinTableName = joinTableInfo.getTableName();

            SqlSegment sqlSegment = sqlSegmentMap.get(joinTableName);
            if (sqlSegment != null) {
                String joinTableAlias = sqlSegment.getTableAlias();
                Set<String> joinTableNames = sqlSegment.getJoinTableNames();
                joinTableNames.add(tableName);

                BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
                String alias = StrUtil.toUnderlineCase(bindingDefinition.getAlias());
                String bindAlias = StrUtil.toUnderlineCase(bindingDefinition.getBindAlias());

                String sqlCriteria = tableAlias + "." + alias + " = " + joinTableAlias + "." + bindAlias;
                JoinSegment joinSegment = new JoinSegment(joinTableName, joinTableAlias, sqlCriteria);
                joinSegments.add(joinSegment);
            }
        }
        return joinSegments;
    }

    private String buildSqlCriteria(String tableAlias, Example example) {
        List<Criterion> criteria = example.getCriteria();
        List<String> sqlCriteria = new ArrayList<>(criteria.size());
        for (Criterion criterion : criteria) {
            sqlCriteria.add(tableAlias + "." + criterion);
        }
        return StrUtil.join(" AND ", sqlCriteria);
    }

    private void markReachableAndDirty(Map<String, SqlSegment> sqlSegmentMap, SqlSegment lastSqlSegment) {
        Set<String> joinTableNames = lastSqlSegment.getJoinTableNames();
        for (String joinTableName : joinTableNames) {
            SqlSegment joinSqlSegment = sqlSegmentMap.get(joinTableName);
            if (joinSqlSegment != null) {
                joinSqlSegment.setRootReachable(true);
                markReachableAndDirty(sqlSegmentMap, joinSqlSegment);
                if (joinSqlSegment.isDirtyQuery()) {
                    lastSqlSegment.setDirtyQuery(true);
                }
            }
        }
    }

    private List<JoinSegment> getAvailableJoinSegments(Map<String, SqlSegment> sqlSegmentMap, SqlSegment sqlSegment) {
        List<JoinSegment> joinSegments = sqlSegment.getJoinSegments();
        List<JoinSegment> availableJoinSegments = new ArrayList<>(joinSegments.size());
        for (JoinSegment joinSegment : joinSegments) {
            String joinTableName = joinSegment.getJoinTableName();
            SqlSegment joinSqlSegment = sqlSegmentMap.get(joinTableName);
            if (joinSqlSegment.isRootReachable() && joinSqlSegment.isDirtyQuery()) {
                availableJoinSegments.add(joinSegment);
            }
        }
        return availableJoinSegments;
    }

}
