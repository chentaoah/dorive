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

import com.gitee.dorive.inject.entity.ExportDefinition;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import com.gitee.dorive.module.entity.ModuleDefinition;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;
import java.util.jar.Manifest;

public class SpringMultiApplication {

    public static final ModuleResourceResolver MODULE_RESOURCE_RESOLVER = new ModuleResourceResolver();
    public static final List<ModuleDefinition> MODULE_DEFINITIONS = new ArrayList<>();

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        MODULE_RESOURCE_RESOLVER.resolveUriManifestMap();

        List<Class<?>> sources = new ArrayList<>();
        sources.add(primarySource);

        Manifest launcherManifest = MODULE_RESOURCE_RESOLVER.getManifest(primarySource);
        Assert.notNull(launcherManifest, "The manifest of launcher cannot be null!");
        ModuleDefinition launcherModuleDefinition = new ModuleDefinition(launcherManifest);
        MODULE_DEFINITIONS.add(launcherModuleDefinition);
        List<String> profiles = new ArrayList<>(launcherModuleDefinition.getProfiles());

        for (String moduleName : launcherModuleDefinition.getDepends()) {
            Manifest moduleManifest = MODULE_RESOURCE_RESOLVER.getManifest(moduleName);
            Assert.notNull(moduleManifest, "The manifest of module cannot be null!");
            ModuleDefinition moduleDefinition = new ModuleDefinition(moduleManifest);
            MODULE_DEFINITIONS.add(moduleDefinition);
            String mainClassName = moduleDefinition.getMainClassName();
            Class<?> clazz = ClassUtil.loadClass(mainClassName);
            if (clazz != null) {
                sources.add(clazz);
                profiles.addAll(moduleDefinition.getProfiles());
            }
        }

        String project = launcherModuleDefinition.getProject();
        Assert.notEmpty(project, "The project name of launcher cannot be empty!");
        Map<String, Object> properties = prepareProperties(project);

        return new SpringApplicationBuilder(sources.toArray(new Class[0]))
                .profiles(profiles.toArray(new String[0]))
                .properties(properties)
                .beanNameGenerator(new ClassNameBeanNameGenerator(project + "."))
                .run(args);
    }

    private static Map<String, Object> prepareProperties(String project) {
        List<com.gitee.dorive.inject.entity.ModuleDefinition> propModuleDefinitions = new ArrayList<>();
        for (ModuleDefinition moduleDefinition : MODULE_DEFINITIONS) {
            com.gitee.dorive.inject.entity.ModuleDefinition propModuleDefinition = new com.gitee.dorive.inject.entity.ModuleDefinition();
            propModuleDefinition.setName(moduleDefinition.getModule());
            propModuleDefinition.setPath(moduleDefinition.getBasePackage() + ".**");

            List<ExportDefinition> exportDefinitions = new ArrayList<>();
            for (String export : moduleDefinition.getExports()) {
                ExportDefinition exportDefinition = new ExportDefinition(export);
                exportDefinitions.add(exportDefinition);
            }
            propModuleDefinition.setExports(exportDefinitions);

            if (!exportDefinitions.isEmpty()) {
                propModuleDefinitions.add(propModuleDefinition);
            }
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        if (!propModuleDefinitions.isEmpty()) {
            properties.put("dorive.enable", true);
            properties.put("dorive.scan", project + ".**");
            properties.put("dorive.modules", propModuleDefinitions);
        }
        return properties;
    }
}
