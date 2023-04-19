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

import java.util.HashSet;

import com.gitee.dorive.spring.boot.starter.entity.OnSegment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.CoatingRepositories;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.PropertyRepository;
import com.gitee.dorive.coating.entity.SpecificProperties;
import com.gitee.dorive.coating.impl.resolver.CoatingRepositoriesResolver;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.CriterionUtils;
import com.gitee.dorive.core.util.SqlUtils;
import com.gitee.dorive.spring.boot.starter.api.Keys;
import com.gitee.dorive.spring.boot.starter.entity.Argument;
import com.gitee.dorive.spring.boot.starter.entity.JoinSegment;
import com.gitee.dorive.spring.boot.starter.entity.Segment;
import com.gitee.dorive.spring.boot.starter.entity.SelectSegment;
import com.gitee.dorive.spring.boot.starter.impl.executor.AliasExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class SQLExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public SQLExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(Context context, Object coating) {
        CoatingRepositoriesResolver coatingRepositoriesResolver = repository.getCoatingRepositoriesResolver();
        Map<String, CoatingRepositories> nameCoatingRepositoriesMap = coatingRepositoriesResolver.getNameCoatingRepositoriesMap();

        CoatingRepositories coatingRepositories = nameCoatingRepositoriesMap.get(coating.getClass().getName());
        Assert.notNull(coatingRepositories, "No coating definition found!");
        List<PropertyRepository> propertyRepositories = coatingRepositories.getPropertyRepositories();

        Map<String, Segment> segmentMap = new LinkedHashMap<>(propertyRepositories.size() * 4 / 3 + 1);
        SelectSegment selectSegment = null;
        char letter = 'a';
        boolean anyDirtyQuery = false;

        for (PropertyRepository propertyRepository : propertyRepositories) {
            MergedRepository mergedRepository = propertyRepository.getMergedRepository();
            String lastAccessPath = mergedRepository.getLastAccessPath();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            String relativeAccessPath = mergedRepository.isMerged() ? absoluteAccessPath + "/" : absoluteAccessPath;
            BinderResolver binderResolver = definedRepository.getBinderResolver();

            Map<String, Object> attachments = executedRepository.getAttachments();
            TableInfo tableInfo = (TableInfo) attachments.get(Keys.TABLE_INFO);
            AliasExecutor aliasExecutor = (AliasExecutor) attachments.get(Keys.ALIAS_EXECUTOR);

            String tableName = tableInfo.getTableName();
            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            Example example = propertyRepository.newExampleByCoating(coating);
            aliasExecutor.convert(example);

            boolean dirtyQuery = example.isDirtyQuery();
            anyDirtyQuery = anyDirtyQuery || dirtyQuery;


            if ("/".equals(relativeAccessPath)) {
                selectSegment = new SelectSegment();
                selectSegment.setReachable(true);
                selectSegment.setDirtyQuery(false);
                selectSegment.setDirectJoinPaths(new LinkedHashSet<>(8));
                selectSegment.setDistinct(true);
                selectSegment.setColumns(Collections.singletonList(tableAlias + ".id"));
                selectSegment.setTableName(tableName);
                selectSegment.setTableAlias(tableAlias);
                segmentMap.put(relativeAccessPath, selectSegment);

            } else {
                JoinSegment joinSegment = new JoinSegment();
                joinSegment.setReachable(false);
                joinSegment.setDirtyQuery(false);
                joinSegment.setDirectJoinPaths(new LinkedHashSet<>(8));
                joinSegment.setTableName(tableName);
                joinSegment.setTableAlias(tableAlias);

                List<OnSegment> onSegments = newOnSegments(segmentMap, lastAccessPath, relativeAccessPath, binderResolver, tableAlias);
                joinSegment.setOnSegments(onSegments);

                if (selectSegment != null) {
                    List<JoinSegment> joinSegments = selectSegment.getJoinSegments();
                    if (joinSegments == null) {
                        joinSegments = new ArrayList<>();
                        selectSegment.setJoinSegments(joinSegments);
                    }
                    joinSegments.add(joinSegment);
                }

                segmentMap.put(relativeAccessPath, joinSegment);
            }
        }

        SpecificProperties properties = coatingRepositories.getSpecificProperties();
        OrderBy orderBy = properties.newOrderBy(coating);
        Page<Object> page = properties.newPage(coating);

        Example example = new Example();
        example.setOrderBy(orderBy);
        example.setPage(page);

        if (rootSqlSegment == null) {
            throw new RuntimeException("Unable to build SQL statement!");
        }
        if (!anyDirtyQuery) {
            return example;
        }
        markReachableAndDirty(segmentMap, rootSqlSegment);
        if (!rootSqlSegment.isDirtyQuery()) {
            return example;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> args = new ArrayList<>();

        buildSQL(sqlBuilder, args, segmentMap);
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

    private List<OnSegment> newOnSegments(Map<String, Segment> segmentMap,
                                          String lastAccessPath,
                                          String relativeAccessPath,
                                          BinderResolver binderResolver,
                                          String tableAlias) {
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        List<OnSegment> onSegments = new ArrayList<>(propertyBinders.size());

        for (PropertyBinder propertyBinder : propertyBinders) {
            String belongAccessPath = propertyBinder.getBelongAccessPath();
            String targetAccessPath = lastAccessPath + belongAccessPath;

            Segment segment = segmentMap.get(targetAccessPath);
            if (segment != null) {
                Set<String> directJoinPaths = segment.getDirectJoinPaths();
                directJoinPaths.add(relativeAccessPath);

                String alias = propertyBinder.getAlias();
                String joinTableAlias = segment.getTableAlias();
                String bindAlias = propertyBinder.getBindAlias();

                OnSegment onSegment = new OnSegment();
                onSegment.setTableAlias(tableAlias);
                onSegment.setColumn(alias);
                onSegment.setJoinTableAlias(joinTableAlias);
                onSegment.setJoinColumn(bindAlias);
                onSegments.add(onSegment);
            }
        }
        return onSegments;
    }

    private void markReachableAndDirty(Map<String, Segment> segmentMap, Segment lastSegment) {
        Set<String> directJoinPaths = lastSegment.getDirectJoinPaths();
        for (String directJoinPath : directJoinPaths) {
            Segment joinSegment = segmentMap.get(directJoinPath);
            if (joinSegment != null) {
                joinSegment.setReachable(true);
                markReachableAndDirty(segmentMap, joinSegment);
                if (joinSegment.isDirtyQuery()) {
                    lastSegment.setDirtyQuery(true);
                }
            }
        }
    }

    private void buildSQL(StringBuilder sqlBuilder, List<Object> args, Map<String, SelectSegment> sqlSegmentMap) {
        List<String> sqlCriteria = new ArrayList<>(sqlSegmentMap.size());

        for (SelectSegment sqlSegment : sqlSegmentMap.values()) {
            if (sqlSegment.isRootReachable() && sqlSegment.isDirtyQuery()) {
                sqlBuilder.append(sqlSegment);

                List<JoinSegment> joinSegments = sqlSegment.getJoinSegments();
                joinSegments = joinSegments.stream().filter(joinSegment -> {
                    SelectSegment joinSqlSegment = sqlSegmentMap.get(joinSegment.getTargetAccessPath());
                    return joinSqlSegment.isRootReachable() && joinSqlSegment.isDirtyQuery();
                }).collect(Collectors.toList());

                if (!joinSegments.isEmpty()) {
                    sqlBuilder.append(StrUtil.join(" AND ", joinSegments)).append(" ");
                }

                String tableAlias = sqlSegment.getTableAlias();
                Example sqlExample = sqlSegment.getExample();
                if (sqlExample != null && sqlExample.isDirtyQuery()) {
                    List<Criterion> criteria = sqlExample.getCriteria();
                    List<Argument> arguments = new ArrayList<>(criteria.size());
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
                            Argument argument = new Argument(property, operator, index);
                            arguments.add(argument);
                        } else {
                            Argument argument = new Argument(property, operator, null);
                            arguments.add(argument);
                        }
                    }
                    String sqlCriterion = CollUtil.join(arguments, " AND ", tableAlias + ".", null);
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
