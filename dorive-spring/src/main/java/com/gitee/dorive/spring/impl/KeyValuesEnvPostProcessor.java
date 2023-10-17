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

package com.gitee.dorive.spring.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.spring.annotation.Key;
import com.gitee.dorive.spring.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class KeyValuesEnvPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Set<String> activeProfiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
        Properties properties = new Properties();
        Class<?> clazz = this.getClass();
        ReflectionUtils.doWithLocalFields(clazz, declaredField -> {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                return;
            }
            Object value = determineValue(environment, activeProfiles, declaredField);
            if (value != null) {
                ReflectUtil.setFieldValue(this, declaredField, value);
                Key keyAnnotation = AnnotationUtils.getAnnotation(declaredField, Key.class);
                if (keyAnnotation != null) {
                    String property = keyAnnotation.value();
                    if (StringUtils.isNotBlank(property) && !environment.containsProperty(property)) {
                        properties.setProperty(property, value.toString());
                    }
                }
            }
        });
        if (!properties.isEmpty()) {
            PropertiesPropertySource propertySource = new PropertiesPropertySource(clazz.getName() + "@KeyValues", properties);
            environment.getPropertySources().addLast(propertySource);
        }
    }

    private Object determineValue(ConfigurableEnvironment environment, Set<String> activeProfiles, Field declaredField) {
        Set<Value> valueAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(declaredField, Value.class);
        if (!valueAnnotations.isEmpty()) {
            for (Value valueAnnotation : valueAnnotations) {
                String profile = valueAnnotation.profile();
                if (StringUtils.isBlank(profile) || activeProfiles.contains(profile)) {
                    String valueStr = valueAnnotation.value();
                    if (StringUtils.isNotBlank(valueStr)) {
                        valueStr = environment.resolvePlaceholders(valueStr);
                        Class<?> type = declaredField.getType();
                        if (type == Boolean.class) {
                            return Boolean.valueOf(valueStr);

                        } else if (type == Integer.class) {
                            return Integer.valueOf(valueStr);

                        } else if (type == String.class) {
                            return valueStr;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
