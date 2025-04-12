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

package com.gitee.dorive.module.impl.parser;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.util.ClassUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class AbstractModuleParser implements ModuleParser {

    public static final PathMatcher PATH_MATCHER = new AntPathMatcher(".");

    private final Map<String, ModuleDefinition> nameModuleDefinitionMap = new ConcurrentHashMap<>();
    private final Map<URI, ModuleDefinition> uriModuleDefinitionMap = new ConcurrentHashMap<>();
    private final Map<String, ModuleDefinition> configModuleDefinitionMap = new ConcurrentHashMap<>();
    private final List<String> scanPackages = new ArrayList<>();

    @Override
    public void parse() {
        parseModuleDefinitions();
        collectScanPackages();
        checkRequiresAndProvides();
    }

    private void parseModuleDefinitions() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/MANIFEST.MF");
            for (Resource resource : resources) {
                URL url = resource.getURL();
                String protocol = url.getProtocol();
                URI uriForMatch;
                if ("file".equals(protocol)) {
                    String path = url.toString();
                    int index = path.indexOf("META-INF/MANIFEST.MF");
                    uriForMatch = new URI(path.substring(0, index));

                } else if ("jar".equals(protocol)) {
                    String path = url.getPath();
                    int index = path.indexOf("!/META-INF/MANIFEST.MF");
                    uriForMatch = new URI(path.substring(0, index));

                } else {
                    continue;
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    Manifest manifest = new Manifest(inputStream);
                    Attributes mainAttributes = manifest.getMainAttributes();
                    String moduleName = mainAttributes.getValue("Dorive-Module");
                    if (moduleName != null) {
                        ModuleDefinition moduleDefinition = new ModuleDefinition(resource, manifest);
                        nameModuleDefinitionMap.put(moduleName, moduleDefinition);
                        uriModuleDefinitionMap.put(uriForMatch, moduleDefinition);
                        List<String> configs = moduleDefinition.getConfigs();
                        if (configs != null && !configs.isEmpty()) {
                            for (String config : configs) {
                                if (StringUtils.isNotBlank(config)) {
                                    configModuleDefinitionMap.put(config, moduleDefinition);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void collectScanPackages() {
        Set<String> scanPackages = nameModuleDefinitionMap.values().stream().map(ModuleDefinition::getScanPackage).collect(Collectors.toSet());
        this.scanPackages.addAll(scanPackages);
    }

    private void checkRequiresAndProvides() {
        Set<String> requires = new HashSet<>();
        Set<String> provides = new HashSet<>();
        for (ModuleDefinition moduleDefinition : getModuleDefinitions()) {
            requires.addAll(moduleDefinition.getRequires());
            provides.addAll(moduleDefinition.getProvides());
        }
        Collection<String> collection = CollectionUtil.subtract(requires, provides);
        if (!collection.isEmpty()) {
            throw new RuntimeException("Lack of required services! service: " + StrUtil.join(", ", collection));
        }
    }

    @Override
    public Set<String> getModuleNames() {
        return nameModuleDefinitionMap.keySet();
    }

    @Override
    public ModuleDefinition getModuleDefinition(String name) {
        return nameModuleDefinitionMap.get(name);
    }

    @Override
    public List<ModuleDefinition> getModuleDefinitions() {
        List<ModuleDefinition> moduleDefinitions = new ArrayList<>(nameModuleDefinitionMap.values());
        moduleDefinitions.sort(Comparator.comparing(ModuleDefinition::getOrder));
        return moduleDefinitions;
    }

    @Override
    public boolean isUnderScanPackage(String className) {
        if (isNotSpringInternalType(className)) {
            for (String scanPackage : scanPackages) {
                if (PATH_MATCHER.match(scanPackage, className)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNotSpringInternalType(String className) {
        return !className.startsWith("org.springframework.");
    }

    @Override
    public ModuleDefinition findModuleDefinition(URI uri) {
        return uri != null ? uriModuleDefinitionMap.get(uri) : null;
    }

    @Override
    public ModuleDefinition findModuleDefinition(Class<?> clazz) {
        return findModuleDefinition(ClassUtils.toURI(clazz));
    }

    @Override
    public ModuleDefinition findModuleDefinitionByConfigName(String configName) {
        return configModuleDefinitionMap.get(configName);
    }

}
