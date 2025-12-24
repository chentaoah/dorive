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

package com.gitee.dorive.mybatis2.v1.impl.querier;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Selector;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultContext;
import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.base.v1.mybatis.api.CountQuerier;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.base.v1.mybatis.entity.CountQuery;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.mybatis2.v1.entity.SelectSegment;
import com.gitee.dorive.mybatis2.v1.entity.TableSegment;
import com.gitee.dorive.query2.v1.api.QueryResolver;
import com.gitee.dorive.query2.v1.entity.segment.SegmentInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DefaultCountQuerier implements CountQuerier {

    private final RepositoryContext repository;
    private final QueryResolver queryResolver;
    private final SqlRunner sqlRunner;

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        Selector selector = countQuery.getSelector();
        if (selector != null) {
            context = new DefaultContext(context);
            context.setOption(Selector.class, selector);
        }

        SegmentInfo segmentInfo = (SegmentInfo) queryResolver.resolve(context, countQuery.getQuery());
        SelectSegment selectSegment = (SelectSegment) segmentInfo.getSegment();
        RepositoryContext selectedRepository = (RepositoryContext) segmentInfo.getRepository();
        String selectRepositoryAlias = segmentInfo.getRepositoryAlias();

        TableSegment tableSegment = selectSegment.getTableSegment();
        List<Object> args = selectSegment.getArgs();

        String tableAlias = tableSegment.getTableAlias();

        // group by
        Translator translator = repository.getProperty(Translator.class);
        List<String> groupBy = toAliases(translator, countQuery.getGroupBy());
        String groupByColumns = CollUtil.join(groupBy, ",", tableAlias + ".", null);
        selectSegment.setGroupBy("GROUP BY " + groupByColumns);

        // count by
        if (selectedRepository != null) {
            translator = selectedRepository.getProperty(Translator.class);
            tableAlias = selectRepositoryAlias;
        }
        List<String> countBy = toAliases(translator, countQuery.getCountBy());
        String countByStr = CollUtil.join(countBy, ",',',", tableAlias + ".", null);
        String countByExp = buildCountByExp(countQuery, countBy, countByStr);

        // select columns
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(groupByColumns);
        selectColumns.add(String.format("COUNT(%s) AS total", countByExp));
        selectSegment.setSelectColumns(selectColumns);

        List<Map<String, Object>> resultMaps = sqlRunner.selectList(selectSegment.toString(), args.toArray());
        Map<String, Long> countMap = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        resultMaps.forEach(resultMap -> countMap.put(buildKey(resultMap, groupBy), (Long) resultMap.get("total")));
        return countMap;
    }

    private List<String> toAliases(Translator translator, List<String> properties) {
        return properties.stream().map(translator::toAlias).collect(Collectors.toList());
    }

    private String buildCountByExp(CountQuery countQuery, List<String> countBy, String countByStr) {
        StringBuilder countByExp = new StringBuilder();
        if (countQuery.isDistinct()) {
            countByExp.append("DISTINCT ");
        }
        if (countBy.size() == 1) {
            countByExp.append(countByStr);

        } else if (countBy.size() > 1) {
            countByExp.append("CONCAT(").append(countByStr).append(")");
        }
        return countByExp.toString();
    }

    private String buildKey(Map<String, Object> resultMap, List<String> groupBy) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String column : groupBy) {
            String valueStr = String.valueOf(resultMap.get(column)).trim();
            keyBuilder.append(valueStr).append(", ");
        }
        int length = keyBuilder.length();
        if (length > 0) {
            keyBuilder.delete(length - 2, length);
        }
        return keyBuilder.toString();
    }

}
