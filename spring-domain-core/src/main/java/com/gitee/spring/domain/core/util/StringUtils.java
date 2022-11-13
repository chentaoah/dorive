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
package com.gitee.spring.domain.core.util;

import cn.hutool.core.util.StrUtil;

import java.util.Collection;

public class StringUtils {

    public static String[] toUnderlineCase(String... columns) {
        String[] newColumns = new String[columns.length];
        for (int index = 0; index < columns.length; index++) {
            newColumns[index] = StrUtil.toUnderlineCase(columns[index]);
        }
        return newColumns;
    }

    public static String[] toStringArray(Object object) {
        if (object instanceof String) {
            return new String[]{(String) object};

        } else if (object instanceof String[]) {
            return (String[]) object;

        } else if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            String[] stringArray = new String[collection.size()];
            int index = 0;
            for (Object item : collection) {
                stringArray[index++] = item.toString();
            }
            return stringArray;
        }
        return null;
    }

}
