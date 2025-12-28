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

package com.gitee.dorive.joiner.v1.impl.keyGen;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.joiner.v1.api.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MultiFieldKeyGenerator implements KeyGenerator {

    private final List<Binder> binders;

    @Override
    public String generate(Context context, Object entity) {
        StringBuilder keyBuilder = new StringBuilder();
        for (Binder binder : binders) {
            Object fieldValue = binder.getFieldValue(context, entity);
            if (fieldValue != null) {
                String key = fieldValue.toString();
                keyBuilder.append("(").append(key.length()).append(")").append(key).append(",");
            } else {
                keyBuilder = null;
                break;
            }
        }
        if (keyBuilder != null && keyBuilder.length() > 0) {
            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            return keyBuilder.toString();
        }
        return null;
    }

}
