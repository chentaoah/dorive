/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.gitee.dorive.module.v1.impl.util.ClassLoaderUtils;
import com.gitee.dorive.module.v1.impl.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ModuleLauncher {

    public static final ModuleLauncher INSTANCE = new ModuleLauncher();

    private final String mavenRepositoryPath;
    private final boolean existMavenRepository;
    private final Set<URI> addedUris;
    private final Set<URL> urlsToLoad;

    public ModuleLauncher() {
        this.mavenRepositoryPath = findMavenRepositoryPath();
        this.existMavenRepository = mavenRepositoryPath != null && FileUtil.exist(mavenRepositoryPath);
        this.addedUris = new HashSet<>();
        this.urlsToLoad = new LinkedHashSet<>();
    }

    private String findMavenRepositoryPath() {
        String mavenRepositoryPath = null;

        String mavenHome = System.getenv("MAVEN_HOME");
        if (StringUtils.isNotBlank(mavenHome)) {
            mavenRepositoryPath = mavenHome + File.separator + "repository";
        }
        if (FileUtil.exist(mavenRepositoryPath)) {
            return mavenRepositoryPath;
        }

        String userHome = System.getProperty("user.home");
        if (StringUtils.isNotBlank(userHome)) {
            mavenRepositoryPath = userHome + File.separator + ".m2" + File.separator + "repository";
        }
        if (FileUtil.exist(mavenRepositoryPath)) {
            return mavenRepositoryPath;
        }

        return null;
    }

    public ClassLoader tryLoadClasspathIdx(Class<?> source) {
        URI sourceUri = ClassUtils.toURI(source);
        if (sourceUri == null) {
            return null;
        }
        String sourceUriStr = sourceUri.toString();
        if (!sourceUriStr.endsWith("/target/classes/")) {
            return null;
        }
        loadClasspathIdx(sourceUri);
        if (urlsToLoad.isEmpty()) {
            return null;
        }
        // 手动创建类加载器
        URLClassLoader urlClassLoader;
        // 上下文中的类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader instanceof URLClassLoader) {
            urlClassLoader = (URLClassLoader) classLoader;
            ClassLoaderUtils.loadUrls(urlClassLoader, urlsToLoad);
        } else {
            urlClassLoader = new URLClassLoader(urlsToLoad.toArray(new URL[0]), classLoader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }
        return urlClassLoader;
    }

    private void loadClasspathIdx(URI targetClassesUri) {
        if (addedUris.add(targetClassesUri)) {
            URI fileUri = targetClassesUri.resolve("META-INF/classpath.idx");
            File file = new File(fileUri);
            if (file.exists()) {
                doLoadClasspathIdx(file);
            }
        }
    }

    private void doLoadClasspathIdx(File file) {
        List<String> lines = FileUtil.readLines(file, StandardCharsets.UTF_8);
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("module:")) {
                URL moduleUrl = handleModule(file, line);
                urlsToLoad.add(moduleUrl);
                loadClasspathIdx(URLUtil.toURI(moduleUrl));

            } else if (line.startsWith("maven:") && existMavenRepository) {
                urlsToLoad.add(handleMaven(line));
            }
        }
    }

    private URL handleModule(File file, String line) {
        URL fileUrl = URLUtil.getURL(file);
        String fileUrlStr = fileUrl.toString();
        String path = line.substring(line.indexOf(":") + 1).trim();
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String project = path.substring(0, path.indexOf("/"));
        String urlPrefix = fileUrlStr.substring(0, fileUrlStr.lastIndexOf("/" + project + "/"));
        return URLUtil.url(urlPrefix + "/" + path);
    }

    private URL handleMaven(String line) {
        String path = line.substring(line.indexOf(":") + 1).trim();
        List<String> strings = StrUtil.splitTrim(path, ":");
        String groupId = strings.get(0);
        String artifactId = strings.get(1);
        String version = strings.get(2);

        String jarPath = StrUtil.replace(groupId, ".", File.separator);
        jarPath = jarPath + File.separator + artifactId + File.separator + version + File.separator + artifactId + "-" + version + ".jar";
        jarPath = mavenRepositoryPath + File.separator + jarPath;
        return URLUtil.url(jarPath);
    }

}
