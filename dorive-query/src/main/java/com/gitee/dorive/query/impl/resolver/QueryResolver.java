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

package com.gitee.dorive.query.impl.resolver;

import com.gitee.dorive.core.entity.executor.*;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.BuildQuery;
import com.gitee.dorive.query.entity.QueryField;
import com.gitee.dorive.query.entity.SpecificFields;
import com.gitee.dorive.query.entity.def.ExampleDef;
import com.gitee.dorive.query.entity.def.CriterionDef;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class QueryResolver {

    private ExampleDef exampleDef;
    private List<QueryField> queryFields;
    private SpecificFields specificFields;
    private List<MergedRepository> mergedRepositories;
    private List<MergedRepository> reversedMergedRepositories;

    public BuildQuery newQuery(Object query) {
        Map<String, Example> exampleMap = newExampleMap(query);

        Example example = exampleMap.computeIfAbsent("/", key -> new InnerExample());
        example.setOrderBy(specificFields.newOrderBy(query));
        example.setPage(specificFields.newPage(query));

        return new BuildQuery(this, query, exampleMap, example, false, false, false);
    }

    private Map<String, Example> newExampleMap(Object query) {
        Map<String, Example> exampleMap = new LinkedHashMap<>(8);
        for (QueryField queryField : queryFields) {
            Object fieldValue = queryField.getFieldValue(query);
            if (fieldValue != null) {
                CriterionDef criterionDef = queryField.getCriterionDef();
                String belongTo = criterionDef.getBelongTo();
                String fieldName = criterionDef.getField();
                String operator = criterionDef.getOperator();
                Example example = exampleMap.computeIfAbsent(belongTo, key -> new InnerExample());
                example.getCriteria().add(new Criterion(fieldName, operator, fieldValue));
            }
        }
        return exampleMap;
    }

}
