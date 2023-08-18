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

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.coating.entity.CoatingCriteria;
import com.gitee.dorive.coating.entity.CoatingType;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.spring.boot.starter.api.Keys;
import com.gitee.dorive.spring.boot.starter.entity.*;
import com.gitee.dorive.spring.boot.starter.impl.executor.AliasExecutor;
import com.gitee.dorive.spring.boot.starter.impl.executor.ConverterExecutor;
import com.gitee.dorive.spring.boot.starter.util.CriterionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class SegmentBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public SegmentResult buildSegment(Context context, Object coating) {
        CoatingType coatingType = repository.getCoatingType(coating);
        CoatingCriteria coatingCriteria = coatingType.newCriteria(coating);
        Map<String, List<Criterion>> criteriaMap = coatingCriteria.getCriteriaMap();
        OrderBy orderBy = coatingCriteria.getOrderBy();
        Page<Object> page = coatingCriteria.getPage();

        List<MergedRepository> mergedRepositories = coatingType.getMergedRepositories();
        Map<String, Segment> segmentMap = new LinkedHashMap<>(mergedRepositories.size() * 4 / 3 + 1);
        char letter = 'a';
        SelectSegment selectSegment = null;
        List<ArgSegment> argSegments = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        for (MergedRepository mergedRepository : mergedRepositories) {
            String lastAccessPath = mergedRepository.getLastAccessPath();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            Map<String, Object> attachments = executedRepository.getAttachments();
            TableInfo tableInfo = (TableInfo) attachments.get(Keys.TABLE_INFO);
            AliasExecutor aliasExecutor = (AliasExecutor) attachments.get(Keys.ALIAS_EXECUTOR);
            ConverterExecutor converterExecutor = (ConverterExecutor) attachments.get(Keys.CONVERTER_EXECUTOR);

            String tableName = tableInfo.getTableName();
            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            List<Criterion> criteria = criteriaMap.computeIfAbsent(absoluteAccessPath, key -> Collections.emptyList());
            converterExecutor.convertCriteria(context, criteria);
            aliasExecutor.convertCriteria(criteria);
            appendArguments(argSegments, args, tableAlias, criteria);

            if ("/".equals(relativeAccessPath)) {
                selectSegment = new SelectSegment();
                selectSegment.setReachable(true);
                selectSegment.setDirtyQuery(!criteria.isEmpty());
                selectSegment.setDirectedSegments(new ArrayList<>(8));
                selectSegment.setDistinct(false);
                selectSegment.setColumns(Collections.emptyList());
                selectSegment.setTableName(tableName);
                selectSegment.setTableAlias(tableAlias);
                selectSegment.setJoinSegments(new ArrayList<>());
                selectSegment.setArgSegments(argSegments);
                segmentMap.put(relativeAccessPath, selectSegment);

                aliasExecutor.convertOrderBy(orderBy);

            } else {
                JoinSegment joinSegment = new JoinSegment();
                joinSegment.setReachable(false);
                joinSegment.setDirtyQuery(!criteria.isEmpty());
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

        if (selectSegment != null) {
            markReachableAndDirty(selectSegment);
        }

        return new SegmentResult(letter, selectSegment, args, orderBy, page);
    }

    private void appendArguments(List<ArgSegment> argSegments,
                                 List<Object> args,
                                 String tableAlias,
                                 List<Criterion> criteria) {
        for (Criterion criterion : criteria) {
            String property = tableAlias + "." + criterion.getProperty();
            String operator = CriterionUtils.getOperator(criterion);
            if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
                ArgSegment argSegment = new ArgSegment(property, operator, null);
                argSegments.add(argSegment);

            } else {
                Object value = criterion.getValue();
                args.add(CriterionUtils.format(operator, value));
                int index = args.size() - 1;
                ArgSegment argSegment = new ArgSegment(property, operator, "{" + index + "}");
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
            String relativeAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
            Segment segment = segmentMap.get(relativeAccessPath);
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
