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

package com.gitee.dorive.module.v1.impl.util;

import org.springframework.util.PropertyPlaceholderHelper;

public class PlaceholderUtils {

    private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER =
            new PropertyPlaceholderHelper("${", "}");

    public static boolean contains(String strValue) {
        int startIndex = strValue.indexOf("${");
        if (startIndex != -1) {
            int endIndex = strValue.indexOf("}", startIndex);
            return endIndex != -1 && startIndex < endIndex;
        }
        return false;
    }

    public static String replace(String strValue, PropertyPlaceholderHelper.PlaceholderResolver resolver) {
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(strValue, resolver);
    }

}
