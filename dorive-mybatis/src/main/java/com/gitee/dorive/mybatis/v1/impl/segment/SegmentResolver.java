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

package com.gitee.dorive.mybatis.v1.impl.segment;

import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.util.CriterionUtils;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.binder.v1.impl.binder.StrongBinder;
import com.gitee.dorive.binder.v1.impl.binder.ValueFilterBinder;
import com.gitee.dorive.binder.v1.impl.binder.ValueRouteBinder;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import com.gitee.dorive.factory.v1.api.EntityMapper;
import com.gitee.dorive.factory.v1.api.EntityMappers;
import com.gitee.dorive.mybatis.v1.api.Segment;
import com.gitee.dorive.mybatis.v1.entity.EntityStoreInfo;
import com.gitee.dorive.mybatis.v1.entity.segment.ArgSegment;
import com.gitee.dorive.mybatis.v1.entity.segment.OnSegment;
import com.gitee.dorive.mybatis.v1.entity.segment.OnValueSegment;
import com.gitee.dorive.mybatis.v1.entity.segment.TableJoinSegment;
import com.gitee.dorive.mybatis.v1.entity.segment.TableSegment;
import com.gitee.dorive.mybatis.v1.enums.Mapper;
import com.gitee.dorive.query.v1.entity.MergedRepository;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.entity.QueryUnit;
import com.gitee.dorive.query.v1.impl.resolver.MergedRepositoryResolver;
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

    private RepositoryContext repository;
    private QueryContext queryContext;
    private Map<String, QueryUnit> queryUnitMap;
    private QueryUnit queryUnit;

    public Segment resolve() {
        MergedRepository mergedRepository = queryUnit.getMergedRepository();
        DefaultRepository defaultRepository = mergedRepository.getDefaultRepository();
        EntityStoreInfo entityStoreInfo = defaultRepository.getProperty(EntityStoreInfo.class);
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
        MergedRepositoryResolver mergedRepositoryResolver = repository.getProperty(MergedRepositoryResolver.class);
        Context context = queryContext.getContext();
        MergedRepository mergedRepository = queryUnit.getMergedRepository();

        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        RepositoryItem definedRepository = mergedRepository.getDefinedRepository();
        Map<String, List<StrongBinder>> mergedStrongBindersMap = mergedRepository.getMergedStrongBindersMap();
        Map<String, List<ValueRouteBinder>> mergedValueRouteBindersMap = mergedRepository.getMergedValueRouteBindersMap();
        EntityMapper entityMapper = getEntityMapper(mergedRepository);

        BinderResolver binderResolver = (BinderResolver) definedRepository.getBinderExecutor();
        List<ValueFilterBinder> valueFilterBinders = binderResolver.getValueFilterBinders();

        List<OnSegment> onSegments = new ArrayList<>(mergedStrongBindersMap.size() + mergedValueRouteBindersMap.size() + valueFilterBinders.size());
        mergedStrongBindersMap.forEach((absoluteAccessPath, strongBinders) -> {
            MergedRepository targetMergedRepository = mergedRepositoryMap.get(absoluteAccessPath);
            EntityMapper targetEntityMapper = getEntityMapper(targetMergedRepository);
            for (StrongBinder strongBinder : strongBinders) {
                String leftExpr = mergedRepository.getAlias() + "." + entityMapper.toAlias(strongBinder.getFieldName());
                String operator = "=";
                String rightExpr = targetMergedRepository.getAlias() + "." + targetEntityMapper.toAlias(strongBinder.getBindField());
                OnSegment onSegment = new OnSegment(leftExpr, operator, rightExpr);
                onSegments.add(onSegment);
            }
        });
        mergedValueRouteBindersMap.forEach((absoluteAccessPath, valueRouteBinders) -> {
            MergedRepository targetMergedRepository = mergedRepositoryMap.get(absoluteAccessPath);
            EntityMapper targetEntityMapper = getEntityMapper(targetMergedRepository);
            for (ValueRouteBinder valueRouteBinder : valueRouteBinders) {
                String leftExpr = targetMergedRepository.getAlias() + "." + targetEntityMapper.toAlias(valueRouteBinder.getBindField());
                String operator = "=";
                String rightExpr = CriterionUtils.sqlParam(valueRouteBinder.getFieldValue(context, null));
                OnValueSegment onValueSegment = new OnValueSegment(leftExpr, operator, rightExpr);
                onSegments.add(onValueSegment);
            }
        });
        for (ValueFilterBinder valueFilterBinder : valueFilterBinders) {
            String leftExpr = mergedRepository.getAlias() + "." + entityMapper.toAlias(valueFilterBinder.getFieldName());
            String operator = "=";
            String rightExpr = CriterionUtils.sqlParam(valueFilterBinder.getBoundValue(context, null));
            OnValueSegment onValueSegment = new OnValueSegment(leftExpr, operator, rightExpr);
            onSegments.add(onValueSegment);
        }
        return onSegments;
    }

    private EntityMapper getEntityMapper(MergedRepository mergedRepository) {
        DefaultRepository defaultRepository = mergedRepository.getDefaultRepository();
        EntityMappers entityMappers = defaultRepository.getProperty(EntityMappers.class);
        return entityMappers.getEntityMapper(Mapper.ENTITY_DATABASE.name());
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
