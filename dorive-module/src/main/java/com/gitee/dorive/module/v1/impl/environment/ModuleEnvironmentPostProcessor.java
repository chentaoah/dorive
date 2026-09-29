/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.environment;

import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.entity.ModuleDefinition;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ModuleEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof OriginTrackedMapPropertySource) {
                String name = propertySource.getName();
                String configName = parseConfigName(name);
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinitionByConfigName(configName);
                if (moduleDefinition != null) {
                    Object source = propertySource.getSource();
                    if (source instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) source;
                        if (!map.isEmpty()) {
                            Map<String, Object> newMap = new LinkedHashMap<>();
                            map.forEach((key, value) -> newMap.put(moduleDefinition.getPropertiesPrefix() + key, value));
                            PropertySource<?> newPropertySource = new OriginTrackedMapPropertySource(name, Collections.unmodifiableMap(newMap));
                            propertySources.replace(name, newPropertySource);
                        }
                    }
                }
            }
        }
    }

    private String parseConfigName(String name) {
        int startIndex = name.indexOf("[");
        int endIndex = name.indexOf("]");
        if (startIndex >= 0 && startIndex < endIndex) {
            return name.substring(startIndex + 1, endIndex);
        }
        return name;
    }

}
