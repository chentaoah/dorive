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

package com.gitee.dorive.binder.v1.impl.union;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.qry.UnionExample;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UnionEntityHandler extends AbstractEntityHandler {

    public UnionEntityHandler(RepositoryItem repository, KeyValueJoiner keyValueJoiner) {
        super(repository, keyValueJoiner);
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
            keyValueJoiner.addLeft(entity, row);
        }
        return unionExample;
    }

    private Example newExample(Context context, Object entity) {
        Example example = new InnerExample();
        BinderResolver binderResolver = (BinderResolver) repository.getBinderExecutor();
        List<Binder> binders = binderResolver.getStrongBinders();
        for (Binder binder : binders) {
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
    protected void handleResult(Context context, Result<Object> result) {
        List<Map<String, Object>> recordMaps = result.getRecordMaps();
        List<Object> entities = result.getRecords();
        Assert.isTrue(recordMaps.size() == entities.size(), "Inconsistent data!");
        for (int index = 0; index < recordMaps.size(); index++) {
            Map<String, Object> resultMap = recordMaps.get(index);
            Object entity = entities.get(index);
            if (entity != null) {
                Object row = resultMap.get("$row");
                List<String> rows = (List<String>) resultMap.get("$rows");
                if (rows != null) {
                    for (String eachRow : rows) {
                        keyValueJoiner.addRight(eachRow, entity);
                    }
                } else if (row != null) {
                    keyValueJoiner.addRight(row.toString(), entity);
                }
            }
        }
    }

}
