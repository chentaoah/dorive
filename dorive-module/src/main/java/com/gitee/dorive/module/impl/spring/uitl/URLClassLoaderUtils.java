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
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class URLClassLoaderUtils {

    public static void tryLoadClasspathIdx(Class<?> source) {
        URI sourceUri = ClassUtils.toURI(source);
        if (sourceUri == null) {
            return;
        }
        String sourceUriStr = sourceUri.toString();
        if (!sourceUriStr.endsWith("/target/classes/")) {
            return;
        }
        URL fileUrl = findResource(sourceUri);
        if (fileUrl == null) {
            return;
        }
        String fileUrlStr = fileUrl.toString();
        if (!fileUrlStr.endsWith("/target/classes/META-INF/classpath.idx")) {
            return;
        }

        String repositoryPath = findMavenRepositoryPath();
        boolean existMavenRepository = FileUtil.exist(repositoryPath);

        List<String> lines = FileUtil.readLines(fileUrl, StandardCharsets.UTF_8);
        List<URL> urls = new ArrayList<>(lines.size());
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("module:")) {
                urls.add(handleModule(fileUrlStr, line));

            } else if (line.startsWith("maven:") && existMavenRepository) {
                urls.add(handleMaven(repositoryPath, line));
            }
        }

        ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private static URL findResource(URI sourceUri) {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/classpath.idx");
            for (Resource resource : resources) {
                URL fileUrl = resource.getURL();
                if ("file".equals(fileUrl.getProtocol())) {
                    String path = fileUrl.toString();
                    int index = path.indexOf("META-INF/classpath.idx");
                    URI fileUri = URLUtil.toURI(path.substring(0, index));
                    if (sourceUri.equals(fileUri)) {
                        return fileUrl;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static String findMavenRepositoryPath() {
        String mavenHome = System.getenv("MAVEN_HOME");
        if (StringUtils.isNotBlank(mavenHome)) {
            return mavenHome + File.separator + "repository";
        }
        String userHome = System.getProperty("user.home");
        if (StringUtils.isNotBlank(userHome)) {
            return userHome + File.separator + ".m2" + File.separator + "repository";
        }
        return null;
    }

    private static URL handleModule(String fileUrlStr, String line) {
        String path = line.substring(line.indexOf(":") + 1).trim();
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String project = path.substring(0, path.indexOf("/"));
        String urlPrefix = fileUrlStr.substring(0, fileUrlStr.indexOf("/" + project + "/"));
        return URLUtil.url(urlPrefix + "/" + path);
    }

    private static URL handleMaven(String repositoryPath, String line) {
        String path = line.substring(line.indexOf(":") + 1).trim();
        List<String> strings = StrUtil.splitTrim(path, ":");
        String groupId = strings.get(0);
        String artifactId = strings.get(1);
        String version = strings.get(2);

        String jarPath = StrUtil.replace(groupId, ".", File.separator);
        jarPath = jarPath + File.separator + artifactId + File.separator + version + File.separator + artifactId + "-" + version + ".jar";
        jarPath = repositoryPath + File.separator + jarPath;
        return URLUtil.url(jarPath);
    }

}
