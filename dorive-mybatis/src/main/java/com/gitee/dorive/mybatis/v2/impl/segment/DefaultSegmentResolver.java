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

package com.gitee.dorive.mybatis.v2.impl.segment;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.util.CriterionUtils;
import com.gitee.dorive.base.v1.factory.api.Transformer;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.mybatis.v2.entity.ArgSegment;
import com.gitee.dorive.mybatis.v2.entity.OnSegment;
import com.gitee.dorive.mybatis.v2.entity.SelectSegment;
import com.gitee.dorive.mybatis.v2.entity.TableJoinSegment;
import com.gitee.dorive.mybatis.v2.entity.TableSegment;
import com.gitee.dorive.query.v2.api.SegmentResolver;
import com.gitee.dorive.query.v2.entity.segment.ConditionInfo;
import com.gitee.dorive.query.v2.entity.segment.JoinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultSegmentResolver implements SegmentResolver {

    @Override
    public Object resolve(Map<RepositoryContext, String> repositoryAliasMap,
                          List<JoinInfo> joinInfos,
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

        List<TableJoinSegment> tableJoinSegments = newTableJoinSegments(repositoryAliasMap, joinInfos, repositoryExampleMap, args);

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
                                                        List<JoinInfo> joinInfos,
                                                        Map<RepositoryContext, Example> repositoryExampleMap,
                                                        List<Object> args) {
        List<TableJoinSegment> tableJoinSegments = new ArrayList<>(joinInfos.size());
        for (JoinInfo joinInfo : joinInfos) {
            RepositoryContext joiner = joinInfo.getJoiner();
            EntityStoreInfo entityStoreInfo = joiner.getProperty(EntityStoreInfo.class);
            String tableName = entityStoreInfo.getTableName();
            String tableAlias = repositoryAliasMap.get(joiner);
            Example example = repositoryExampleMap.get(joiner);

            TableJoinSegment tableJoinSegment = new TableJoinSegment(newOnSegments(repositoryAliasMap, joinInfo));
            tableJoinSegment.setTableName(tableName);
            tableJoinSegment.setTableAlias(tableAlias);
            tableJoinSegment.setArgSegments(newArgSegments(tableAlias, example, args));
            tableJoinSegments.add(tableJoinSegment);
        }
        return tableJoinSegments;
    }

    private List<OnSegment> newOnSegments(Map<RepositoryContext, String> repositoryAliasMap, JoinInfo joinInfo) {
        List<ConditionInfo> conditionInfos = joinInfo.getConditionInfos();
        List<OnSegment> onSegments = new ArrayList<>(conditionInfos.size());
        for (ConditionInfo conditionInfo : conditionInfos) {
            RepositoryContext source = conditionInfo.getSource();
            String sourceField = conditionInfo.getSourceField();
            RepositoryContext target = conditionInfo.getTarget();
            String targetField = conditionInfo.getTargetField();
            String literal = conditionInfo.getLiteral();

            String sourceTableAlias = repositoryAliasMap.get(source);
            String sourceFieldAlias = source.getProperty(Transformer.class).toAlias(sourceField);
            String leftExpr = sourceTableAlias + "." + sourceFieldAlias;

            String rightExpr = literal;
            if (target != null) {
                String targetTableAlias = repositoryAliasMap.get(target);
                String targetFieldAlias = target.getProperty(Transformer.class).toAlias(targetField);
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
            Object value = criterion.getValue();

            String leftExpr = tableAlias + "." + property;
            String rightExpr;
            if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
                rightExpr = null;

            } else if (Operator.IN.equals(operator) || Operator.NOT_IN.equals(operator)) {
                // 20260406 ct 适配mybatis-plus3.5.13版本，手动拼接IN、NOT_IN
                rightExpr = CriterionUtils.getValue(criterion);

            } else {
                args.add(CriterionUtils.format(operator, value));
                rightExpr = "{" + (args.size() - 1) + "}";
            }
            ArgSegment argSegment = new ArgSegment(leftExpr, operator, rightExpr);
            argSegments.add(argSegment);
        }
        return argSegments;
    }

}
