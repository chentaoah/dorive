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

import cn.hutool.core.collection.CollUtil;
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
import com.gitee.dorive.sql.entity.ArgSegment;
import com.gitee.dorive.sql.entity.BuildResult;
import com.gitee.dorive.sql.entity.JoinSegment;
import com.gitee.dorive.sql.entity.OnSegment;
import com.gitee.dorive.sql.entity.SelectSegment;
import com.gitee.dorive.sql.entity.TableSegment;
import com.gitee.dorive.sql.util.CriterionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class SegmentBuilder {

    public BuildResult buildSegment(Context context, BuildQuery buildQuery) {
        QueryResolver queryResolver = buildQuery.getQueryResolver();
        Map<String, Example> exampleMap = buildQuery.getExampleMap();

        List<MergedRepository> mergedRepositories = queryResolver.getMergedRepositories();
        Map<String, Node> nodeMap = new LinkedHashMap<>(mergedRepositories.size() * 4 / 3 + 1);

        SelectSegment selectSegment = new SelectSegment();
        List<Object> args = new ArrayList<>();
        char letter = 'a';

        for (MergedRepository mergedRepository : mergedRepositories) {
            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            String lastAccessPath = mergedRepository.getLastAccessPath();
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();

            BinderResolver binderResolver = definedRepository.getBinderResolver();

            Map<String, Object> attachments = executedRepository.getAttachments();
            String tableName = (String) attachments.get(Keys.TABLE_NAME);
            FieldExecutor fieldExecutor = (FieldExecutor) attachments.get(Keys.FIELD_EXECUTOR);

            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample(Collections.emptyList()));
            fieldExecutor.convert(context, example);

            TableSegment tableSegment = new TableSegment(tableName, tableAlias);
            Node node = new Node(tableSegment, example.isNotEmpty(), new ArrayList<>(4));
            nodeMap.put(relativeAccessPath, node);

            if ("/".equals(relativeAccessPath)) {
                node.setDirty(true);
                selectSegment.setTableSegment(tableSegment);

            } else {
                List<OnSegment> onSegments = newOnSegments(nodeMap, lastAccessPath, binderResolver, tableAlias, node);
                if (onSegments.isEmpty()) {
                    nodeMap.remove(relativeAccessPath);
                    continue;
                }
                JoinSegment joinSegment = new JoinSegment(tableSegment, onSegments);
                List<JoinSegment> joinSegments = selectSegment.getJoinSegments();
                joinSegments.add(joinSegment);
            }

            appendArguments(selectSegment, args, tableAlias, example);
        }

        markDirty(nodeMap.get("/"));
        Set<TableSegment> tableSegments = new LinkedHashSet<>();
        nodeMap.forEach((path, node) -> {
            if (node.isDirty()) {
                tableSegments.add(node.getTableSegment());
            }
        });
        List<JoinSegment> joinSegments = selectSegment.getJoinSegments();
        joinSegments = CollUtil.filter(joinSegments, joinSegment -> tableSegments.contains(joinSegment.getTableSegment()));
        selectSegment.setJoinSegments(joinSegments);

        return new BuildResult(selectSegment, args, letter);
    }

    private List<OnSegment> newOnSegments(Map<String, Node> nodeMap, String lastAccessPath, BinderResolver binderResolver, String tableAlias, Node node) {
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

                OnSegment onSegment = new OnSegment();
                onSegment.setTableAlias(tableAlias);
                onSegment.setColumn(propertyBinder.getAlias());
                onSegment.setJoinTableAlias(tableSegment.getTableAlias());
                onSegment.setJoinColumn(propertyBinder.getBindAlias());
                onSegments.add(onSegment);
            }
        }
        return onSegments;
    }

    private void appendArguments(SelectSegment selectSegment, List<Object> args, String tableAlias, Example example) {
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
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

    private void markDirty(Node node) {
        if (node != null) {
            List<Node> children = node.getChildren();
            for (Node child : children) {
                markDirty(child);
                if (child.isDirty()) {
                    node.setDirty(true);
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class Node {
        private TableSegment tableSegment;
        private boolean dirty;
        private List<Node> children;
    }

}
