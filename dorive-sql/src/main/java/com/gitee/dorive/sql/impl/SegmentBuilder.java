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

package com.gitee.dorive.sql.impl;

import com.gitee.dorive.api.constant.Keys;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.executor.FieldExecutor;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.BuildQuery;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.sql.entity.*;
import com.gitee.dorive.sql.util.CriterionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
public class SegmentBuilder {

    public SelectSegment buildSegment(Context context, BuildQuery buildQuery) {
        QueryResolver queryResolver = buildQuery.getQueryResolver();
        Map<String, Example> exampleMap = buildQuery.getExampleMap();

        List<MergedRepository> mergedRepositories = queryResolver.getMergedRepositories();
        Map<String, Node> nodeMap = new LinkedHashMap<>(mergedRepositories.size() * 4 / 3 + 1);
        SelectSegment selectSegment = new SelectSegment();
        List<JoinSegment> joinSegments = selectSegment.getJoinSegments();

        for (MergedRepository mergedRepository : mergedRepositories) {
            String tableAlias = selectSegment.generateTableAlias();

            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            Map<String, Object> attachments = executedRepository.getAttachments();
            String tableName = (String) attachments.get(Keys.TABLE_NAME);
            FieldExecutor fieldExecutor = (FieldExecutor) attachments.get(Keys.FIELD_EXECUTOR);

            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample(Collections.emptyList()));
            fieldExecutor.convert(context, example);

            TableSegment tableSegment = new TableSegment(tableName, tableAlias, example.isNotEmpty(), new ArrayList<>(example.getCriteria().size()));
            Node node = new Node(tableSegment, new ArrayList<>(4));
            nodeMap.put(relativeAccessPath, node);

            if ("/".equals(relativeAccessPath)) {
                selectSegment.setTableSegment(tableSegment);

            } else {
                List<OnSegment> onSegments = newOnSegments(nodeMap, mergedRepository, tableAlias, node);
                if (onSegments.isEmpty()) {
                    nodeMap.remove(relativeAccessPath);
                    continue;
                }
                JoinSegment joinSegment = new JoinSegment(tableSegment, onSegments);
                joinSegments.add(joinSegment);
            }

            addTableSegmentArgs(selectSegment, tableAlias, example, tableSegment);
        }

        markTableSegmentJoin(nodeMap.get("/"));
        selectSegment.filterTableSegments();
        return selectSegment;
    }

    private List<OnSegment> newOnSegments(Map<String, Node> nodeMap, MergedRepository mergedRepository, String tableAlias, Node node) {
        String lastAccessPath = mergedRepository.getLastAccessPath();
        CommonRepository definedRepository = mergedRepository.getDefinedRepository();
        BinderResolver binderResolver = definedRepository.getBinderResolver();
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();

        List<OnSegment> onSegments = new ArrayList<>(propertyBinders.size());
        for (PropertyBinder propertyBinder : propertyBinders) {
            String relativeAccessPath = lastAccessPath + propertyBinder.getBelongAccessPath();
            Node targetNode = nodeMap.get(relativeAccessPath);
            if (targetNode != null) {
                TableSegment tableSegment = targetNode.getTableSegment();
                List<Node> children = targetNode.getChildren();
                if (!children.contains(node)) {
                    children.add(node);
                }
                OnSegment onSegment = new OnSegment(tableAlias, propertyBinder.getAlias(),
                        tableSegment.getTableAlias(), propertyBinder.getBindAlias());
                onSegments.add(onSegment);
            }
        }
        return onSegments;
    }

    private void addTableSegmentArgs(SelectSegment selectSegment, String tableAlias, Example example, TableSegment tableSegment) {
        List<Object> args = selectSegment.getArgs();
        List<ArgSegment> argSegments = tableSegment.getArgSegments();
        for (Criterion criterion : example.getCriteria()) {
            String property = criterion.getProperty();
            String operator = CriterionUtils.getOperator(criterion);
            if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
                ArgSegment argSegment = new ArgSegment(tableAlias, property, operator, null);
                argSegments.add(argSegment);

            } else {
                Object value = criterion.getValue();
                args.add(CriterionUtils.format(operator, value));
                int index = args.size() - 1;
                ArgSegment argSegment = new ArgSegment(tableAlias, property, operator, "{" + index + "}");
                argSegments.add(argSegment);
            }
        }
    }

    private void markTableSegmentJoin(Node node) {
        if (node != null) {
            TableSegment tableSegment = node.getTableSegment();
            List<Node> children = node.getChildren();
            for (Node child : children) {
                markTableSegmentJoin(child);
                TableSegment childTableSegment = child.getTableSegment();
                if (childTableSegment.isJoin()) {
                    tableSegment.setJoin(true);
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class Node {
        private TableSegment tableSegment;
        private List<Node> children;
    }

}
