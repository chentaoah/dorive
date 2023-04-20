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
import cn.hutool.db.sql.SqlBuilder;
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
import com.gitee.dorive.spring.boot.starter.entity.ArgSegment;
import com.gitee.dorive.spring.boot.starter.entity.JoinSegment;
import com.gitee.dorive.spring.boot.starter.entity.OnSegment;
import com.gitee.dorive.spring.boot.starter.entity.Segment;
import com.gitee.dorive.spring.boot.starter.entity.SelectSegment;
import com.gitee.dorive.spring.boot.starter.impl.executor.AliasExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        char letter = 'a';
        SelectSegment selectSegment = null;
        List<ArgSegment> argSegments = new ArrayList<>();
        List<Object> args = new ArrayList<>();

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

            appendArguments(argSegments, args, tableAlias, example);

            if ("/".equals(relativeAccessPath)) {
                selectSegment = new SelectSegment();
                selectSegment.setReachable(true);
                selectSegment.setDirtyQuery(example.isDirtyQuery());
                selectSegment.setDirectedSegments(new ArrayList<>(8));
                selectSegment.setDistinct(true);
                selectSegment.setColumns(Collections.singletonList(tableAlias + ".id"));
                selectSegment.setTableName(tableName);
                selectSegment.setTableAlias(tableAlias);
                selectSegment.setJoinSegments(new ArrayList<>());
                selectSegment.setArgSegments(argSegments);
                segmentMap.put(relativeAccessPath, selectSegment);

            } else {
                JoinSegment joinSegment = new JoinSegment();
                joinSegment.setReachable(false);
                joinSegment.setDirtyQuery(example.isDirtyQuery());
                joinSegment.setDirectedSegments(new ArrayList<>(4));
                joinSegment.setTableName(tableName);
                joinSegment.setTableAlias(tableAlias);

                List<OnSegment> onSegments = newOnSegments(segmentMap, lastAccessPath, binderResolver, tableAlias, joinSegment);
                joinSegment.setOnSegments(onSegments);

                if (selectSegment != null) {
                    List<JoinSegment> joinSegments = selectSegment.getJoinSegments();
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

        if (selectSegment == null) {
            throw new RuntimeException("Unable to build SQL statement!");
        }

        if (argSegments.isEmpty()) {
            return example;
        }

        markReachableAndDirty(selectSegment);

        if (!selectSegment.isDirtyQuery()) {
            return example;
        }

        SqlBuilder builder = selectSegment.createBuilder();

        if (page != null) {
            long count = SqlRunner.db().selectCount("SELECT COUNT(1) FROM (" + builder + ") " + letter, args.toArray());
            page.setTotal(count);
            example.setCountQueried(true);
            if (count == 0) {
                example.setEmptyQuery(true);
                return example;
            }
        }

        if (orderBy != null) {
            builder.append(" ").append(orderBy.toString());
        }
        if (page != null) {
            builder.append(" ").append(page.toString());
        }

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(builder.toString(), args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            example.eq("id", primaryKeys);
        } else {
            example.setEmptyQuery(true);
        }

        return example;
    }

    private void appendArguments(List<ArgSegment> argSegments, List<Object> args, String tableAlias, Example example) {
        List<Criterion> criteria = example.getCriteria();
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
                ArgSegment argSegment = new ArgSegment(tableAlias + "." + property, operator, "{" + index + "}");
                argSegments.add(argSegment);
            } else {
                ArgSegment argSegment = new ArgSegment(tableAlias + "." + property, operator, null);
                argSegments.add(argSegment);
            }
        }
    }

    private List<OnSegment> newOnSegments(Map<String, Segment> segmentMap,
                                          String lastAccessPath,
                                          BinderResolver binderResolver,
                                          String tableAlias,
                                          JoinSegment joinSegment) {
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        List<OnSegment> onSegments = new ArrayList<>(propertyBinders.size());

        for (PropertyBinder propertyBinder : propertyBinders) {
            String belongAccessPath = propertyBinder.getBelongAccessPath();
            String targetAccessPath = lastAccessPath + belongAccessPath;

            Segment segment = segmentMap.get(targetAccessPath);
            if (segment != null) {
                String alias = propertyBinder.getAlias();
                String joinTableAlias = segment.getTableAlias();
                String bindAlias = propertyBinder.getBindAlias();

                OnSegment onSegment = new OnSegment();
                onSegment.setDirectedSegments(Collections.singletonList(segment));
                onSegment.setTableAlias(tableAlias);
                onSegment.setColumn(alias);
                onSegment.setJoinTableAlias(joinTableAlias);
                onSegment.setJoinColumn(bindAlias);
                onSegments.add(onSegment);
                
                List<Segment> directedSegments = segment.getDirectedSegments();
                if (!directedSegments.contains(joinSegment)) {
                    directedSegments.add(joinSegment);
                }
            }
        }
        return onSegments;
    }

    private void markReachableAndDirty(Segment lastSegment) {
        List<Segment> directedSegments = lastSegment.getDirectedSegments();
        for (Segment directedSegment : directedSegments) {
            directedSegment.setReachable(true);
            markReachableAndDirty(directedSegment);
            if (directedSegment.isDirtyQuery()) {
                lastSegment.setDirtyQuery(true);
            }
        }
    }

}
