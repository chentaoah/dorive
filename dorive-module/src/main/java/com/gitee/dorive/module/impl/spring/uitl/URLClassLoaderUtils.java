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

package com.gitee.dorive.module.impl.spring.uitl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class URLClassLoaderUtils {

    public static void tryLoadClasspathIdx(Class<?> source) {
        URL fileUrl = source.getResource("/META-INF/classpath.idx");
        if (fileUrl != null) {
            String fileUrlStr = fileUrl.toString();
            if (!fileUrlStr.endsWith("/target/classes/META-INF/classpath.idx")) {
                return;
            }
            List<String> lines = FileUtil.readLines(fileUrl, StandardCharsets.UTF_8);
            List<URL> urls = new ArrayList<>(lines.size());
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("module:")) {
                    String path = line.substring(line.indexOf(":") + 1).trim();
                    if (!path.endsWith("/")) {
                        path = path + "/";
                    }
                    String project = path.substring(0, path.indexOf("/"));
                    String urlPrefix = fileUrlStr.substring(0, fileUrlStr.indexOf("/" + project + "/"));
                    urls.add(URLUtil.url(urlPrefix + "/" + path));
                }
            }
            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]));
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

}
