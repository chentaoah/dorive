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
package com.gitee.spring.boot.starter.dorive.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.CoatingWrapper;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.RepositoryWrapper;
import com.gitee.dorive.coating.entity.SpecificProperties;
import com.gitee.dorive.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.BindingDefinition;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import com.gitee.dorive.core.util.CriterionUtils;
import com.gitee.spring.boot.starter.dorive.entity.ArgSegment;
import com.gitee.spring.boot.starter.dorive.entity.JoinSegment;
import com.gitee.spring.boot.starter.dorive.entity.Metadata;
import com.gitee.spring.boot.starter.dorive.entity.SqlSegment;

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

        Map<String, SqlSegment> sqlSegmentMap = new LinkedHashMap<>(repositoryWrappers.size() * 4 / 3 + 1);
        SqlSegment rootSqlSegment = null;
        char letter = 'a';

        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            MergedRepository mergedRepository = repositoryWrapper.getMergedRepository();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            ConfiguredRepository definedRepository = mergedRepository.getDefinedRepository();
            ConfiguredRepository configuredRepository = mergedRepository.getConfiguredRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            TableInfo tableInfo = getTableInfo(configuredRepository);
            String tableName = tableInfo.getTableName();

            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            Example example = repositoryWrapper.newExampleByCoating(boundedContext, coatingObject);
            boolean dirtyQuery = example.isDirtyQuery();
            Set<String> joinTableNames = new HashSet<>(8);

            if ("/".equals(absoluteAccessPath)) {
                String sql = String.format("SELECT %s.id FROM %s %s ", tableAlias, tableName, tableAlias);
                rootSqlSegment = new SqlSegment(tableName, tableAlias, sql, Collections.emptyList(), example, true, dirtyQuery, joinTableNames);
                sqlSegmentMap.put(tableName, rootSqlSegment);

            } else {
                String sql = String.format("LEFT JOIN %s %s ON ", tableName, tableAlias);
                List<JoinSegment> joinSegments = newJoinSegments(sqlSegmentMap, binderResolver, tableName, tableAlias);
                SqlSegment sqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, example, false, dirtyQuery, joinTableNames);
                sqlSegmentMap.put(tableName, sqlSegment);
            }
        }

        SpecificProperties properties = coatingWrapper.getSpecificProperties();
        OrderBy orderBy = properties.getOrderBy(coatingObject);
        Page<Object> page = properties.getPage(coatingObject);

        Example example = new Example();
        example.setOrderBy(orderBy);
        example.setUsedPage(page != null);
        example.setPage(page);

        assert rootSqlSegment != null;
        markReachableAndDirty(sqlSegmentMap, rootSqlSegment);
        if (!rootSqlSegment.isDirtyQuery()) {
            return example;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> args = new ArrayList<>();

        buildSQL(sqlBuilder, args, sqlSegmentMap);
        if (page != null) {
            int count = SqlRunner.db().selectCount("SELECT COUNT(1) FROM (" + sqlBuilder + ") " + letter, args.toArray());
            if (count == 0) {
                example.setEmptyQuery(true);
                return example;
            } else {
                page.setTotal(count);
            }
        }

        buildSQL(sqlBuilder, orderBy, page);
        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(sqlBuilder.toString(), args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            example.eq("id", primaryKeys);
        } else {
            example.setEmptyQuery(true);
        }

        return example;
    }

    private TableInfo getTableInfo(ConfiguredRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private List<JoinSegment> newJoinSegments(Map<String, SqlSegment> sqlSegmentMap, BinderResolver binderResolver, String tableName, String tableAlias) {
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

    private void buildSQL(StringBuilder sqlBuilder, List<Object> args, Map<String, SqlSegment> sqlSegmentMap) {
        List<String> sqlCriteria = new ArrayList<>(sqlSegmentMap.size());

        for (SqlSegment sqlSegment : sqlSegmentMap.values()) {
            if (sqlSegment.isRootReachable() && sqlSegment.isDirtyQuery()) {
                sqlBuilder.append(sqlSegment);

                List<JoinSegment> joinSegments = sqlSegment.getJoinSegments();
                joinSegments = joinSegments.stream().filter(joinSegment -> {
                    SqlSegment joinSqlSegment = sqlSegmentMap.get(joinSegment.getJoinTableName());
                    return joinSqlSegment.isRootReachable() && joinSqlSegment.isDirtyQuery();
                }).collect(Collectors.toList());

                if (!joinSegments.isEmpty()) {
                    sqlBuilder.append(StrUtil.join(" AND ", joinSegments)).append(" ");
                }

                String tableAlias = sqlSegment.getTableAlias();
                Example sqlExample = sqlSegment.getExample();
                if (sqlExample != null && sqlExample.isDirtyQuery()) {
                    List<Criterion> criteria = sqlExample.getCriteria();
                    List<ArgSegment> argSegments = new ArrayList<>(criteria.size());
                    for (Criterion criterion : criteria) {
                        String property = CriterionUtils.getProperty(criterion);
                        String operator = CriterionUtils.getOperator(criterion);
                        if (!operator.startsWith("IS")) {
                            args.add(criterion.getValue());
                            int index = args.size() - 1;
                            ArgSegment argSegment = new ArgSegment(property, operator, index);
                            argSegments.add(argSegment);
                        } else {
                            ArgSegment argSegment = new ArgSegment(property, operator, null);
                            argSegments.add(argSegment);
                        }
                    }
                    String sqlCriterion = CollUtil.join(argSegments, " AND ", tableAlias + ".", null);
                    sqlCriteria.add(sqlCriterion);
                }
            }
        }

        sqlBuilder.append("WHERE ").append(StrUtil.join(" AND ", sqlCriteria));
    }

    private void buildSQL(StringBuilder sqlBuilder, OrderBy orderBy, Page<Object> page) {
        if (orderBy != null) {
            sqlBuilder.append(" ").append(orderBy);
        }
        if (page != null) {
            sqlBuilder.append(" ").append(page);
        }
    }

}
