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

package com.gitee.dorive.core.impl.mapper;

import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.EntityMappers;
import com.gitee.dorive.core.api.mapper.FieldMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.*;

@Getter
@AllArgsConstructor
public class DefaultEntityMappers implements EntityMappers {
    private final Map<String, EntityMapper> mapperEntityMapperMap;
    private final List<FieldMapper> valueObjFields;
    private final List<FieldMapper> matchedValueObjFields;
    private final List<FieldMapper> unmatchedValueObjFields;
    private final Set<Type> valueObjTypes;

    @Override
    public EntityMapper getEntityMapper(String mapper) {
        return mapperEntityMapperMap.get(mapper);
    }

    @Override
    public boolean isValueObjType(Type type) {
        return valueObjTypes.contains(type);
    }
}
