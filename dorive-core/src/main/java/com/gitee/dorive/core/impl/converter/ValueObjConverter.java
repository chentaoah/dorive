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

package com.gitee.dorive.core.impl.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.dorive.core.api.factory.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ValueObjConverter implements Converter {

    private Class<?> entityClass;
    private boolean isJson;

    @Override
    public Object reconstitute(Object value) {
        if (value instanceof String) {
            return JSONUtil.toBean((String) value, entityClass);

        } else if (value instanceof Map) {
            return BeanUtil.toBean(value, entityClass);
        }
        return value;
    }

    @Override
    public Object deconstruct(Object value) {
        if (isJson) {
            return JSONUtil.toJsonStr(value);
        } else {
            return BeanUtil.beanToMap(value);
        }
    }

}
