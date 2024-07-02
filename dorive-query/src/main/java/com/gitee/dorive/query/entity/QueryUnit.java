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

import com.gitee.dorive.api.entity.ele.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.executor.ExampleExecutor;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class QueryUnit {

    private MergedRepository mergedRepository;
    private Example example;
    private boolean abandoned;

    public Example getConvertedExample(Context context) {
        CommonRepository executedRepository = mergedRepository.getExecutedRepository();
        EntityElement entityElement = executedRepository.getEntityElement();
        Map<String, Object> attributes = entityElement.getAttributes();
        EntityStoreInfo entityStoreInfo = (EntityStoreInfo) attributes.get(EntityStoreInfo.class.getName());
        ExampleExecutor exampleExecutor = (ExampleExecutor) attributes.get(ExampleExecutor.class.getName());

        exampleExecutor.convert(context, example);
        return example;
    }

}
