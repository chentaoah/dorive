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

import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.util.CriterionUtils;
import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.base.v1.factory.api.TranslatorManager;
import com.gitee.dorive.base.v1.factory.enums.Category;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.mybatis2.v1.entity.*;
import com.gitee.dorive.query2.v1.api.SegmentExecutor;
import com.gitee.dorive.query2.v1.entity.segment.Condition;
import com.gitee.dorive.query2.v1.entity.segment.RepositoryJoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultSegmentExecutor implements SegmentExecutor {

    @Override
    public List<Object> executeQuery(Map<RepositoryContext, String> repositoryAliasMap,
                                     List<RepositoryJoin> repositoryJoins,
                                     Map<RepositoryContext, Example> repositoryExampleMap,
                                     RepositoryContext repositoryContext,
                                     Example example) {
        EntityStoreInfo entityStoreInfo = getEntityStoreInfo(repositoryContext);
        String tableName = entityStoreInfo.getTableName();
        String tableAlias = repositoryAliasMap.get(repositoryContext);
        List<Object> args = new ArrayList<>();

        TableSegment tableSegment = new TableSegment();
        tableSegment.setTableName(tableName);
        tableSegment.setTableAlias(tableAlias);
        tableSegment.setArgSegments(newArgSegments(tableAlias, example, args));

        List<TableJoinSegment> tableJoinSegments = newTableJoinSegments(repositoryAliasMap, repositoryJoins, repositoryExampleMap, args);

        SelectSegment selectSegment = new SelectSegment();
        selectSegment.setTableSegment(tableSegment);
        selectSegment.getTableJoinSegments().addAll(tableJoinSegments);
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        argSegments.addAll(tableSegment.getArgSegments());
        for (TableJoinSegment tableJoinSegment : tableJoinSegments) {
            argSegments.addAll(tableJoinSegment.getArgSegments());
        }
        selectSegment.setArgs(args);


        return null;
    }

    private EntityStoreInfo getEntityStoreInfo(RepositoryContext repositoryContext) {
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        DefaultRepository defaultRepository = (DefaultRepository) rootRepository.getProxyRepository();
        return defaultRepository.getProperty(EntityStoreInfo.class);
    }

    private Translator getTranslator(RepositoryContext repositoryContext) {
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        DefaultRepository defaultRepository = (DefaultRepository) rootRepository.getProxyRepository();
        TranslatorManager translatorManager = defaultRepository.getProperty(TranslatorManager.class);
        return translatorManager.getTranslator(Category.ENTITY_DATABASE.name());
    }

    private List<TableJoinSegment> newTableJoinSegments(Map<RepositoryContext, String> repositoryAliasMap,
                                                        List<RepositoryJoin> repositoryJoins,
                                                        Map<RepositoryContext, Example> repositoryExampleMap,
                                                        List<Object> args) {
        List<TableJoinSegment> tableJoinSegments = new ArrayList<>(repositoryJoins.size());
        for (RepositoryJoin repositoryJoin : repositoryJoins) {
            RepositoryContext joiner = repositoryJoin.getJoiner();
            EntityStoreInfo entityStoreInfo = getEntityStoreInfo(joiner);
            String tableName = entityStoreInfo.getTableName();
            String tableAlias = repositoryAliasMap.get(joiner);
            Example example = repositoryExampleMap.get(joiner);

            TableJoinSegment tableJoinSegment = new TableJoinSegment(newOnSegments(repositoryAliasMap, repositoryJoin));
            tableJoinSegment.setTableName(tableName);
            tableJoinSegment.setTableAlias(tableAlias);
            tableJoinSegment.setArgSegments(newArgSegments(tableAlias, example, args));
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
            String sourceFieldAlias = getTranslator(source).toAlias(sourceField);
            String leftExpr = sourceTableAlias + "." + sourceFieldAlias;
            String operator = "=";

            OnSegment onSegment = null;
            if (target != null) {
                String targetTableAlias = repositoryAliasMap.get(target);
                String targetFieldAlias = getTranslator(target).toAlias(targetField);
                String rightExpr = targetTableAlias + "." + targetFieldAlias;
                onSegment = new OnSegment(leftExpr, operator, rightExpr);

            } else if (literal != null) {
                onSegment = new OnValueSegment(leftExpr, operator, literal);
            }
            if (onSegment != null) {
                onSegments.add(onSegment);
            }
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
