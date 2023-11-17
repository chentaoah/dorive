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

package com.gitee.dorive.query.entity;

import com.gitee.dorive.query.entity.def.ExampleDef;
import com.gitee.dorive.query.entity.def.CriterionDef;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CoatingType {

    private ExampleDef exampleDef;
    private List<CoatingField> coatingFields;
    private SpecificFields specificFields;
    private List<MergedRepository> mergedRepositories;
    private List<MergedRepository> reversedMergedRepositories;

    public CoatingCriteria newCriteria(Object coating) {
        Map<String, List<Criterion>> criteriaMap = newCriteriaMap(coating);
        OrderBy orderBy = specificFields.newOrderBy(coating);
        Page<Object> page = specificFields.newPage(coating);
        return new CoatingCriteria(criteriaMap, orderBy, page);
    }

    public Map<String, List<Criterion>> newCriteriaMap(Object coating) {
        Map<String, List<Criterion>> criteriaMap = new LinkedHashMap<>(8);
        for (CoatingField coatingField : coatingFields) {
            Object fieldValue = coatingField.getFieldValue(coating);
            if (fieldValue != null) {
                CriterionDef criterionDef = coatingField.getCriterionDef();
                String belongTo = criterionDef.getBelongTo();
                String fieldName = criterionDef.getField();
                String operator = criterionDef.getOperator();
                List<Criterion> criteria = criteriaMap.computeIfAbsent(belongTo, key -> new ArrayList<>(4));
                criteria.add(new Criterion(fieldName, operator, fieldValue));
            }
        }
        return criteriaMap;
    }

}
