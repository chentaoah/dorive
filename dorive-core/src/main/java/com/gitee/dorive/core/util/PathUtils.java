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

package com.gitee.dorive.core.util;

import cn.hutool.core.util.URLUtil;

import java.util.Set;

public class PathUtils {

    public static String getAbsolutePath(String accessPath, String relativePath) {
        accessPath = "https://dorive" + accessPath;
        accessPath = URLUtil.completeUrl(accessPath, relativePath);
        return accessPath.replace("https://dorive", "");
    }

    public static String getBelongPath(Set<String> allAccessPath, String accessPath) {
        while (!allAccessPath.contains(accessPath) && !"/".equals(accessPath)) {
            accessPath = PathUtils.getPrevPath(accessPath);
        }
        return accessPath;
    }

    public static String getPrevPath(String accessPath) {
        return accessPath.lastIndexOf("/") > 0 ? accessPath.substring(0, accessPath.lastIndexOf("/")) : "/";
    }

    public static String getLastName(String accessPath) {
        return accessPath.startsWith("/") && accessPath.length() > 1 ? accessPath.substring(accessPath.lastIndexOf("/") + 1) : "";
    }

}
