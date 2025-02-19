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

package com.gitee.dorive.module.impl;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ModuleResourceResolver {

    private final Map<URI, Manifest> uriManifestMap = new HashMap<>();
    private final Map<String, Manifest> moduleManifestMap = new HashMap<>();

    public void resolveManifestMap() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/MANIFEST.MF");
            for (Resource resource : resources) {
                URL manifestUrl = resource.getURL();
                String protocol = manifestUrl.getProtocol();
                String manifestPath;
                int lastIndex;
                if ("file".equals(protocol)) {
                    manifestPath = manifestUrl.toString();
                    lastIndex = manifestPath.indexOf("META-INF/MANIFEST.MF");

                } else if ("jar".equals(protocol)) {
                    manifestPath = manifestUrl.getPath();
                    lastIndex = manifestPath.indexOf("!/META-INF/MANIFEST.MF");

                } else {
                    continue;
                }
                String jarPath = manifestPath.substring(0, lastIndex);
                URI jarUri = new URI(jarPath);
                try (InputStream inputStream = resource.getInputStream()) {
                    Manifest manifest = new Manifest(inputStream);
                    uriManifestMap.put(jarUri, manifest);
                    Attributes mainAttributes = manifest.getMainAttributes();
                    String module = mainAttributes.getValue("Dorive-Module");
                    if (module != null) {
                        moduleManifestMap.put(module, manifest);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Manifest getManifest(Class<?> clazz) {
        try {
            ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                URI codeJarUri = codeSource.getLocation().toURI();
                if ("jar".equals(codeJarUri.getScheme())) {
                    String newPath = codeJarUri.getSchemeSpecificPart();
                    String suffix = "!/BOOT-INF/classes!/";
                    if (newPath.endsWith(suffix)) {
                        newPath = newPath.substring(0, newPath.length() - suffix.length());
                    }
                    if (newPath.endsWith("!/")) {
                        newPath = newPath.substring(0, newPath.length() - 2);
                    }
                    codeJarUri = new URI(newPath);
                }
                return uriManifestMap.get(codeJarUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getNames() {
        return moduleManifestMap.keySet();
    }

    public Manifest getManifest(String module) {
        return moduleManifestMap.get(module);
    }
}
