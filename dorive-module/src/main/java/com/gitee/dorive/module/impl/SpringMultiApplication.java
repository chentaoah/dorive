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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.dorive.inject.config.DoriveInjectionConfiguration;
import com.gitee.dorive.inject.entity.ExportDefinition;
import com.gitee.dorive.module.entity.ModuleDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class SpringMultiApplication {

    public static final ModuleResourceResolver MODULE_RESOURCE_RESOLVER = new ModuleResourceResolver();
    public static final List<ModuleDefinition> MODULE_DEFINITIONS = new ArrayList<>();

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        MODULE_RESOURCE_RESOLVER.resolveManifestMap();

        Set<Class<?>> sources = new LinkedHashSet<>();
        Set<String> profiles = new LinkedHashSet<>();

        Set<String> names = MODULE_RESOURCE_RESOLVER.getNames();
        for (String name : names) {
            Manifest manifest = MODULE_RESOURCE_RESOLVER.getManifest(name);
            Assert.notNull(manifest, "The manifest of module cannot be null!");
            ModuleDefinition moduleDefinition = new ModuleDefinition(manifest);
            MODULE_DEFINITIONS.add(moduleDefinition);
        }
        MODULE_DEFINITIONS.sort(Comparator.comparing(ModuleDefinition::getOrder));

        for (ModuleDefinition moduleDefinition : MODULE_DEFINITIONS) {
            Class<?> mainClass = moduleDefinition.getMainClass();
            if (mainClass != null) {
                sources.add(mainClass);
            }
            profiles.addAll(moduleDefinition.getProfiles());
        }

        sources.add(primarySource);

        String scanPackages = getScanPackages();
        Map<String, Object> properties = prepareProperties(scanPackages);
        BeanNameGenerator beanNameGenerator = new ClassNameBeanNameGenerator(scanPackages);

        return new SpringApplicationBuilder(sources.toArray(new Class[0]))
                .profiles(profiles.toArray(new String[0]))
                .properties(properties)
                .beanNameGenerator(beanNameGenerator)
                .run(args);
    }

    private static String getScanPackages() {
        Set<String> scanPackages = MODULE_DEFINITIONS.stream().map(ModuleDefinition::getProject).collect(Collectors.toSet());
        return CollUtil.join(scanPackages, ", ", null, ".**");
    }

    private static Map<String, Object> prepareProperties(String scanPackages) {
        List<com.gitee.dorive.inject.entity.ModuleDefinition> propModuleDefinitions = new ArrayList<>();
        for (ModuleDefinition moduleDefinition : MODULE_DEFINITIONS) {
            com.gitee.dorive.inject.entity.ModuleDefinition propModuleDefinition = new com.gitee.dorive.inject.entity.ModuleDefinition();
            propModuleDefinition.setName(moduleDefinition.getModule());
            propModuleDefinition.setPath(moduleDefinition.getBasePackage() + ".**");

            List<String> exports = moduleDefinition.getExports();
            if (exports != null && !exports.isEmpty()) {
                List<ExportDefinition> exportDefinitions = new ArrayList<>(exports.size());
                for (String export : exports) {
                    ExportDefinition exportDefinition = new ExportDefinition(export);
                    exportDefinitions.add(exportDefinition);
                }
                propModuleDefinition.setExports(exportDefinitions);
            } else {
                propModuleDefinition.setExports(Collections.emptyList());
            }

            propModuleDefinitions.add(propModuleDefinition);
        }
        if (!propModuleDefinitions.isEmpty()) {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put(DoriveInjectionConfiguration.DORIVE_ENABLE_KEY, true);
            properties.put(DoriveInjectionConfiguration.DORIVE_SCAN_KEY, scanPackages);
            properties.put(DoriveInjectionConfiguration.DORIVE_MODULES_KEY, propModuleDefinitions);
            return properties;
        }
        return Collections.emptyMap();
    }
}
