/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.api;

import com.gitee.dorive.module.v1.entity.ModuleDefinition;
import org.springframework.boot.ApplicationArguments;

import java.net.URI;
import java.util.List;
import java.util.Set;

public interface ModuleParser {

    void parse(ClassLoader classLoader, ApplicationArguments args);

    Set<String> getModuleNames();

    ModuleDefinition getModuleDefinition(String name);

    List<ModuleDefinition> getModuleDefinitions();

    boolean isUnderScanPackage(String className);

    ModuleDefinition findModuleDefinition(URI uri);

    ModuleDefinition findModuleDefinition(Class<?> clazz);

    ModuleDefinition findModuleDefinitionByConfigName(String configName);

}
