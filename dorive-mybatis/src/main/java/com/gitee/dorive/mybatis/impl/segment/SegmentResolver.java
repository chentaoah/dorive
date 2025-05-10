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

package com.gitee.dorive.mybatis.impl.segment;

import com.gitee.dorive.api.constant.core.Operator;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.binder.ValueFilterBinder;
import com.gitee.dorive.core.impl.binder.ValueRouteBinder;
import com.gitee.dorive.core.impl.repository.CommonRepository;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.util.CriterionUtils;
import com.gitee.dorive.mybatis.api.Segment;
import com.gitee.dorive.mybatis.entity.segment.*;
import com.gitee.dorive.mybatis.impl.repository.DefaultStoreRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class SegmentResolver {

    private AbstractQueryRepository<?, ?> repository;
    private QueryContext queryContext;
    private Map<String, QueryUnit> queryUnitMap;
    private QueryUnit queryUnit;

    public Segment resolve() {
        MergedRepository mergedRepository = queryUnit.getMergedRepository();
        DefaultStoreRepository defaultStoreRepository = (DefaultStoreRepository) mergedRepository.getDefaultRepository();
        EntityStoreInfo entityStoreInfo = defaultStoreRepository.getEntityStoreInfo();
        String tableName = entityStoreInfo.getTableName();
        return newTableSegment(tableName);
    }

    private TableSegment newTableSegment(String tableName) {
        MergedRepository mergedRepository = queryUnit.getMergedRepository();
        Example example = queryUnit.getExample();

        String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
        TableSegment tableSegment = "/".equals(absoluteAccessPath) ? new TableSegment() : new TableJoinSegment(newOnSegments());
        tableSegment.setTableName(tableName);
        tableSegment.setTableAlias(mergedRepository.getAlias());
        tableSegment.setJoin(example.isNotEmpty());
        tableSegment.setArgSegments(newArgSegments());
        if (tableSegment.isJoin()) {
            setJoinForBound(mergedRepository);
        }
        return tableSegment;
    }

    private List<OnSegment> newOnSegments() {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Context context = queryContext.getContext();
        MergedRepository mergedRepository = queryUnit.getMergedRepository();

        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        CommonRepository definedRepository = mergedRepository.getDefinedRepository();
        Map<String, List<StrongBinder>> mergedStrongBindersMap = mergedRepository.getMergedStrongBindersMap();
        Map<String, List<ValueRouteBinder>> mergedValueRouteBindersMap = mergedRepository.getMergedValueRouteBindersMap();

        BinderResolver binderResolver = definedRepository.getBinderResolver();
        List<ValueFilterBinder> valueFilterBinders = binderResolver.getValueFilterBinders();

        List<OnSegment> onSegments = new ArrayList<>(mergedStrongBindersMap.size() + mergedValueRouteBindersMap.size() + valueFilterBinders.size());
        mergedStrongBindersMap.forEach((absoluteAccessPath, strongBinders) -> {
            MergedRepository targetMergedRepository = mergedRepositoryMap.get(absoluteAccessPath);
            for (StrongBinder strongBinder : strongBinders) {
                String leftExpr = mergedRepository.getAlias() + "." + strongBinder.getFieldAlias();
                String operator = "=";
                String rightExpr = targetMergedRepository.getAlias() + "." + strongBinder.getBindFieldAlias();
                OnSegment onSegment = new OnSegment(leftExpr, operator, rightExpr);
                onSegments.add(onSegment);
            }
        });
        mergedValueRouteBindersMap.forEach((absoluteAccessPath, valueRouteBinders) -> {
            MergedRepository targetMergedRepository = mergedRepositoryMap.get(absoluteAccessPath);
            for (ValueRouteBinder valueRouteBinder : valueRouteBinders) {
                String leftExpr = targetMergedRepository.getAlias() + "." + valueRouteBinder.getBindFieldAlias();
                String operator = "=";
                String rightExpr = CriterionUtils.sqlParam(valueRouteBinder.getFieldValue(context, null));
                OnValueSegment onValueSegment = new OnValueSegment(leftExpr, operator, rightExpr);
                onSegments.add(onValueSegment);
            }
        });
        for (ValueFilterBinder valueFilterBinder : valueFilterBinders) {
            String leftExpr = mergedRepository.getAlias() + "." + valueFilterBinder.getFieldAlias();
            String operator = "=";
            String rightExpr = CriterionUtils.sqlParam(valueFilterBinder.getBoundValue(context, null));
            OnValueSegment onValueSegment = new OnValueSegment(leftExpr, operator, rightExpr);
            onSegments.add(onValueSegment);
        }
        return onSegments;
    }

    private List<ArgSegment> newArgSegments() {
        List<Object> args = queryContext.getArgs();
        MergedRepository mergedRepository = queryUnit.getMergedRepository();
        Example example = queryUnit.getExample();

        String tableAlias = mergedRepository.getAlias();
        List<ArgSegment> argSegments = new ArrayList<>(example.getCriteria().size());
        for (Criterion criterion : example.getCriteria()) {
            String property = criterion.getProperty();
            String operator = CriterionUtils.getOperator(criterion);
            if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
                ArgSegment argSegment = new ArgSegment(tableAlias + "." + property, operator, null);
                argSegments.add(argSegment);

            } else {
                Object value = criterion.getValue();
                args.add(CriterionUtils.format(operator, value));
                int index = args.size() - 1;
                ArgSegment argSegment = new ArgSegment(tableAlias + "." + property, operator, "{" + index + "}");
                argSegments.add(argSegment);
            }
        }
        return argSegments;
    }

    private void setJoinForBound(MergedRepository mergedRepository) {
        Set<String> boundAccessPaths = mergedRepository.getBoundAccessPaths();
        for (String boundAccessPath : boundAccessPaths) {
            QueryUnit queryUnit = queryUnitMap.get(boundAccessPath);
            TableSegment tableSegment = (TableSegment) queryUnit.getAttachment();
            tableSegment.setJoin(true);
            setJoinForBound(queryUnit.getMergedRepository());
        }
    }
}
