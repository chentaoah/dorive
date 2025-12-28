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
