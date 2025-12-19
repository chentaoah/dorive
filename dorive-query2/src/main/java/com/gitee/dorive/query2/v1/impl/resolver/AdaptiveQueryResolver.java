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

package com.gitee.dorive.query2.v1.impl.resolver;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.query.enums.QueryMode;
import com.gitee.dorive.query2.v1.api.QueryResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AdaptiveQueryResolver implements QueryResolver {

    private final Map<QueryMode, QueryResolver> queryResolverMap;

    @Override
    public Example newExample(Context context, Object query) {
        QueryMode queryMode = context.getOption(QueryMode.class);
        if (queryMode == null) {
            queryMode = QueryMode.STEPWISE;
        }
        QueryResolver queryResolver = queryResolverMap.get(queryMode);
        return queryResolver.newExample(context, query);
    }

}
