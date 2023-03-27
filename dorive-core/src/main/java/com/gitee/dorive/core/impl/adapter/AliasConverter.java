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

package com.gitee.dorive.core.impl.adapter;

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.UnionExample;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AliasConverter {

    private EntityEle entityEle;

    public void convert(Example example) {
        List<String> selectColumns = example.getSelectColumns();
        if (selectColumns != null && !selectColumns.isEmpty()) {
            selectColumns = entityEle.toAliases(selectColumns);
            example.selectColumns(selectColumns);
        }

        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String property = criterion.getProperty();
                property = entityEle.toAlias(property);
                criterion.setProperty(property);
            }
        }

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            List<String> orderByColumns = orderBy.getColumns();
            orderByColumns = entityEle.toAliases(orderByColumns);
            orderBy.setColumns(orderByColumns);
        }
    }

    public void convert(UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        for (Example example : examples) {
            convert(example);
        }
    }

}