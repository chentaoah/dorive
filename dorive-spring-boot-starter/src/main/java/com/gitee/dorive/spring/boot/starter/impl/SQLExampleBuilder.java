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
package com.gitee.dorive.spring.boot.starter.impl;

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
import com.gitee.dorive.core.api.constant.Operator;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.AliasConverter;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.CriterionUtils;
import com.gitee.dorive.core.util.SqlUtils;
import com.gitee.dorive.spring.boot.starter.entity.ArgSegment;
import com.gitee.dorive.spring.boot.starter.entity.JoinSegment;
import com.gitee.dorive.spring.boot.starter.entity.Metadata;
import com.gitee.dorive.spring.boot.starter.entity.SqlSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public SQLExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(Context context, Object coatingObject) {
        CoatingWrapperResolver coatingWrapperResolver = repository.getCoatingWrapperResolver();
        Map<String, CoatingWrapper> nameCoatingWrapperMap = coatingWrapperResolver.getNameCoatingWrapperMap();

        CoatingWrapper coatingWrapper = nameCoatingWrapperMap.get(coatingObject.getClass().getName());
        Assert.notNull(coatingWrapper, "No coating wrapper exists!");
        List<RepositoryWrapper> repositoryWrappers = coatingWrapper.getRepositoryWrappers();

        Map<String, SqlSegment> sqlSegmentMap = new LinkedHashMap<>(repositoryWrappers.size() * 4 / 3 + 1);
        SqlSegment rootSqlSegment = null;
        char letter = 'a';
        boolean anyDirtyQuery = false;

        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            MergedRepository mergedRepository = repositoryWrapper.getMergedRepository();
            String lastAccessPath = mergedRepository.getLastAccessPath();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository commonRepository = mergedRepository.getCommonRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();
            AliasConverter aliasConverter = commonRepository.getAliasConverter();

            TableInfo tableInfo = getTableInfo(commonRepository);
            String tableName = tableInfo.getTableName();

            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            Example example = repositoryWrapper.newExampleByCoating(context, coatingObject);
            aliasConverter.convert(example);

            boolean dirtyQuery = example.isDirtyQuery();
            anyDirtyQuery = anyDirtyQuery || dirtyQuery;

            Set<String> joinTableNames = new HashSet<>(8);

            if ("/".equals(absoluteAccessPath)) {
                String sql = String.format("SELECT %s.id FROM %s %s ", tableAlias, tableName, tableAlias);
                rootSqlSegment = new SqlSegment(tableName, tableAlias, sql, Collections.emptyList(), example, true, dirtyQuery, joinTableNames);
                sqlSegmentMap.put(absoluteAccessPath, rootSqlSegment);

            } else {
                String sql = String.format("LEFT JOIN %s %s ON ", tableName, tableAlias);
                List<JoinSegment> joinSegments = newJoinSegments(sqlSegmentMap, lastAccessPath, absoluteAccessPath, binderResolver, tableAlias);
                SqlSegment sqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, example, false, dirtyQuery, joinTableNames);
                sqlSegmentMap.put(absoluteAccessPath, sqlSegment);
            }
        }

        SpecificProperties properties = coatingWrapper.getSpecificProperties();
        OrderBy orderBy = properties.newOrderBy(coatingObject);
        Page<Object> page = properties.newPage(coatingObject);

        Example example = new Example();
        example.setOrderBy(orderBy);
        example.setPage(page);

        if (rootSqlSegment == null) {
            throw new RuntimeException("Unable to build SQL statement!");
        }
        if (!anyDirtyQuery) {
            return example;
        }
        markReachableAndDirty(sqlSegmentMap, rootSqlSegment);
        if (!rootSqlSegment.isDirtyQuery()) {
            return example;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> args = new ArrayList<>();

        buildSQL(sqlBuilder, args, sqlSegmentMap);
        if (page != null) {
            long count = SqlRunner.db().selectCount("SELECT COUNT(1) FROM (" + sqlBuilder + ") " + letter, args.toArray());
            page.setTotal(count);
            example.setCountQueried(true);
            if (count == 0) {
                example.setEmptyQuery(true);
                return example;
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

    private TableInfo getTableInfo(CommonRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private List<JoinSegment> newJoinSegments(Map<String, SqlSegment> sqlSegmentMap, String lastAccessPath, String absoluteAccessPath, BinderResolver binderResolver, String tableAlias) {
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        List<JoinSegment> joinSegments = new ArrayList<>(propertyBinders.size());

        for (PropertyBinder propertyBinder : propertyBinders) {
            String belongAccessPath = propertyBinder.getBelongAccessPath();
            String targetAccessPath = lastAccessPath + belongAccessPath;

            SqlSegment sqlSegment = sqlSegmentMap.get(targetAccessPath);
            if (sqlSegment != null) {
                Set<String> targetAccessPaths = sqlSegment.getTargetAccessPaths();
                targetAccessPaths.add(absoluteAccessPath);

                String joinTableName = sqlSegment.getTableName();
                String joinTableAlias = sqlSegment.getTableAlias();

                String alias = propertyBinder.getAlias();
                String bindAlias = propertyBinder.getBindAlias();
                String sqlCriteria = tableAlias + "." + alias + " = " + joinTableAlias + "." + bindAlias;

                JoinSegment joinSegment = new JoinSegment(targetAccessPath, joinTableName, joinTableAlias, sqlCriteria);
                joinSegments.add(joinSegment);
            }
        }
        return joinSegments;
    }

    private void markReachableAndDirty(Map<String, SqlSegment> sqlSegmentMap, SqlSegment lastSqlSegment) {
        Set<String> targetAccessPaths = lastSqlSegment.getTargetAccessPaths();
        for (String targetAccessPath : targetAccessPaths) {
            SqlSegment joinSqlSegment = sqlSegmentMap.get(targetAccessPath);
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
                    SqlSegment joinSqlSegment = sqlSegmentMap.get(joinSegment.getTargetAccessPath());
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
                        String property = criterion.getProperty();
                        String operator = CriterionUtils.getOperator(criterion);
                        Object value = criterion.getValue();
                        if (operator.endsWith(Operator.LIKE)) {
                            value = SqlUtils.toLike(value);
                        }
                        if (!operator.startsWith("IS")) {
                            args.add(value);
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
