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

package com.gitee.dorive.launcher.v1.impl.querier;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.factory.v1.api.EntityMapper;
import com.gitee.dorive.factory.v1.api.EntityMappers;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.repository.v1.api.CountQuerier;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.base.v1.factory.enums.Category;
import com.gitee.dorive.mybatis.v1.entity.SelectSegment;
import com.gitee.dorive.mybatis.v1.entity.TableSegment;
import com.gitee.dorive.repository.v1.entity.sql.CountQuery;
import com.gitee.dorive.repository.v1.impl.repository.AbstractMybatisRepository;
import com.gitee.dorive.mybatis.v1.impl.segment.SelectSegmentBuilder;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.entity.MergedRepository;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.entity.QueryUnit;
import com.gitee.dorive.base.v1.query.enums.QueryMode;
import com.gitee.dorive.query.v1.enums.ResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SqlCountQuerier implements CountQuerier {

    private final AbstractMybatisRepository<?, ?> repository;
    private final QueryHandler queryHandler;
    private final SqlRunner sqlRunner;

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        Object query = countQuery.getQuery();
        QueryContext queryContext = new QueryContext(context, query.getClass(), ResultType.COUNT);

        context.setOption(QueryMode.class, QueryMode.SQL_BUILD);
        queryHandler.handle(queryContext, query);

        QueryUnit queryUnit = queryContext.getQueryUnit();
        TableSegment tableSegment = (TableSegment) queryUnit.getAttachment();
        String tableAlias = tableSegment.getTableAlias();

        SelectSegmentBuilder selectSegmentBuilder = new SelectSegmentBuilder(repository, queryContext);
        List<QueryUnit> queryUnits = selectSegmentBuilder.select(countQuery.getSelector());
        SelectSegment selectSegment = selectSegmentBuilder.build();
        List<Object> args = selectSegment.getArgs();

        // group by
        List<String> groupBy = toAliases(queryUnit, countQuery.getGroupBy());
        String groupByColumns = CollUtil.join(groupBy, ",", tableAlias + ".", null);
        selectSegment.setGroupBy("GROUP BY " + groupByColumns);

        // count by
        QueryUnit selectQueryUnit = queryUnits != null && !queryUnits.isEmpty() ? queryUnits.get(0) : queryUnit;
        String countByExp = buildCountByExp(countQuery, selectQueryUnit);

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

    private List<String> toAliases(QueryUnit queryUnit, List<String> properties) {
        MergedRepository mergedRepository = queryUnit.getMergedRepository();
        DefaultRepository defaultRepository = mergedRepository.getDefaultRepository();
        EntityMappers entityMappers = defaultRepository.getProperty(EntityMappers.class);
        EntityMapper entityMapper = entityMappers.getEntityMapper(Category.ENTITY_DATABASE.name());
        return entityMapper.toAliases(properties);
    }

    private String buildCountByExp(CountQuery countQuery, QueryUnit queryUnit) {
        TableSegment tableSegment = (TableSegment) queryUnit.getAttachment();
        String tableAlias = tableSegment.getTableAlias();

        List<String> countBy = toAliases(queryUnit, countQuery.getCountBy());
        String countByStr = CollUtil.join(countBy, ",',',", tableAlias + ".", null);

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
