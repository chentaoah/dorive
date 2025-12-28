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

package com.gitee.dorive.mybatis2.v1.impl.segment;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.util.CriterionUtils;
import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.mybatis2.v1.entity.*;
import com.gitee.dorive.query2.v1.api.SegmentResolver;
import com.gitee.dorive.query2.v1.entity.segment.Condition;
import com.gitee.dorive.query2.v1.entity.segment.RepositoryJoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultSegmentResolver implements SegmentResolver {

    @Override
    public Object resolve(Map<RepositoryContext, String> repositoryAliasMap,
                          List<RepositoryJoin> repositoryJoins,
                          Map<RepositoryContext, Example> repositoryExampleMap,
                          RepositoryContext repositoryContext,
                          Example example) {
        EntityStoreInfo entityStoreInfo = repositoryContext.getProperty(EntityStoreInfo.class);
        String tableName = entityStoreInfo.getTableName();
        String tableAlias = repositoryAliasMap.get(repositoryContext);
        List<Object> args = new ArrayList<>();

        TableSegment tableSegment = new TableSegment();
        tableSegment.setTableName(tableName);
        tableSegment.setTableAlias(tableAlias);
        tableSegment.setArgSegments(newArgSegments(tableAlias, example, args));

        List<TableJoinSegment> tableJoinSegments = newTableJoinSegments(repositoryAliasMap, repositoryJoins, repositoryExampleMap, args);

        SelectSegment selectSegment = new SelectSegment();
        // from table
        selectSegment.setTableSegment(tableSegment);
        // left join table
        selectSegment.getTableJoinSegments().addAll(tableJoinSegments);
        // where column = {0}
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        argSegments.addAll(tableSegment.getArgSegments());
        for (TableJoinSegment tableJoinSegment : tableJoinSegments) {
            argSegments.addAll(tableJoinSegment.getArgSegments());
        }
        // [1, 2, 3]
        selectSegment.setArgs(args);
        return selectSegment;
    }

    private List<TableJoinSegment> newTableJoinSegments(Map<RepositoryContext, String> repositoryAliasMap,
                                                        List<RepositoryJoin> repositoryJoins,
                                                        Map<RepositoryContext, Example> repositoryExampleMap,
                                                        List<Object> args) {
        List<TableJoinSegment> tableJoinSegments = new ArrayList<>(repositoryJoins.size());
        for (RepositoryJoin repositoryJoin : repositoryJoins) {
            RepositoryContext joiner = repositoryJoin.getJoiner();
            EntityStoreInfo entityStoreInfo = joiner.getProperty(EntityStoreInfo.class);
            String tableName = entityStoreInfo.getTableName();
            String tableAlias = repositoryAliasMap.get(joiner);
            Example example = repositoryExampleMap.get(joiner);

            TableJoinSegment tableJoinSegment = new TableJoinSegment(newOnSegments(repositoryAliasMap, repositoryJoin));
            tableJoinSegment.setTableName(tableName);
            tableJoinSegment.setTableAlias(tableAlias);
            tableJoinSegment.setArgSegments(newArgSegments(tableAlias, example, args));
            tableJoinSegments.add(tableJoinSegment);
        }
        return tableJoinSegments;
    }

    private List<OnSegment> newOnSegments(Map<RepositoryContext, String> repositoryAliasMap, RepositoryJoin repositoryJoin) {
        List<Condition> conditions = repositoryJoin.getConditions();
        List<OnSegment> onSegments = new ArrayList<>(conditions.size());
        for (Condition condition : conditions) {
            RepositoryContext source = condition.getSource();
            String sourceField = condition.getSourceField();
            RepositoryContext target = condition.getTarget();
            String targetField = condition.getTargetField();
            String literal = condition.getLiteral();

            String sourceTableAlias = repositoryAliasMap.get(source);
            String sourceFieldAlias = source.getProperty(Translator.class).toAlias(sourceField);
            String leftExpr = sourceTableAlias + "." + sourceFieldAlias;

            String rightExpr = literal;
            if (target != null) {
                String targetTableAlias = repositoryAliasMap.get(target);
                String targetFieldAlias = target.getProperty(Translator.class).toAlias(targetField);
                rightExpr = targetTableAlias + "." + targetFieldAlias;
            }
            Assert.notNull(rightExpr, "The rightExpr cannot be null!");

            onSegments.add(new OnSegment(leftExpr, "=", rightExpr));
        }
        return onSegments;
    }

    private List<ArgSegment> newArgSegments(String tableAlias, Example example, List<Object> args) {
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

}
