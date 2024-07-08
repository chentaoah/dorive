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

package com.gitee.dorive.core.impl.joiner;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UnionEntityJoiner extends AbstractEntityJoiner {

    public UnionEntityJoiner(CommonRepository repository, int entitiesSize) {
        super(repository, entitiesSize);
    }

    @Override
    public Example newExample(Context context, List<Object> entities) {
        UnionExample unionExample = new UnionExample();
        for (int index = 0; index < entities.size(); index++) {
            Object entity = entities.get(index);
            Example example = newExample(context, entity);
            if (example.isEmpty()) {
                continue;
            }
            String row = Integer.toString(index + 1);
            example.setSelectSuffix(row + " as $row");
            unionExample.addExample(example);
            addToRootIndex(entity, row);
        }
        return unionExample;
    }

    private Example newExample(Context context, Object entity) {
        Example example = new InnerExample();
        List<StrongBinder> binders = repository.getBinderResolver().getStrongBinders();
        for (StrongBinder binder : binders) {
            Object boundValue = binder.getBoundValue(context, entity);
            boundValue = binder.input(context, boundValue);
            if (boundValue instanceof Collection) {
                if (((Collection<?>) boundValue).isEmpty()) {
                    boundValue = null;
                }
            }
            if (boundValue != null) {
                String fieldName = binder.getFieldName();
                example.eq(fieldName, boundValue);
            } else {
                example.getCriteria().clear();
                break;
            }
        }
        appendFilterCriteria(context, example);
        return example;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void buildRecordIndex(Context context, List<Object> entities, Result<Object> result) {
        List<Map<String, Object>> recordMaps = result.getRecordMaps();
        for (Map<String, Object> resultMap : recordMaps) {
            Object row = resultMap.get("$row");
            List<String> rows = (List<String>) resultMap.get("$rows");
            Object entity = resultMap.get("$entity");
            if (entity != null) {
                if (rows != null) {
                    for (String eachRow : rows) {
                        addToRecordIndex(eachRow, entity);
                    }
                } else if (row != null) {
                    addToRecordIndex(row.toString(), entity);
                }
            }
        }
    }

}
