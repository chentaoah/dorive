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

package com.gitee.dorive.factory.v1.impl.mapper;

import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.factory.v1.api.EntityTranslator;
import com.gitee.dorive.factory.v1.api.EntityTranslatorManager;
import com.gitee.dorive.factory.v1.api.FieldMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class DefaultEntityTranslatorManager implements EntityTranslatorManager {
    private final Map<String, EntityTranslator> categoryEntityTranslatorMap;
    private final List<FieldMapper> valueObjFields;
    private final List<FieldMapper> matchedValueObjFields;
    private final List<FieldMapper> unmatchedValueObjFields;
    private final Set<Type> valueObjTypes;

    @Override
    public Translator getTranslator(String category) {
        return categoryEntityTranslatorMap.get(category);
    }

    @Override
    public boolean isValueObjType(Type type) {
        return valueObjTypes.contains(type);
    }
}
