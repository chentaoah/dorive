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

package com.gitee.dorive.core.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StringUtils {

    public static List<String> toList(Object object) {
        if (object instanceof String) {
            List<String> list = new ArrayList<>(1);
            list.add((String) object);
            return list;

        } else if (object instanceof String[]) {
            return new ArrayList<>(Arrays.asList((String[]) object));

        } else if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            List<String> list = new ArrayList<>(collection.size());
            for (Object item : collection) {
                list.add(item.toString());
            }
            return list;
        }
        return null;
    }

}
