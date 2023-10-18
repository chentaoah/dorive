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

package com.gitee.dorive.env.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.env.annotation.Key;
import com.gitee.dorive.env.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class KeyValuesEnvPostProcessor implements EnvironmentPostProcessor, Ordered {

    protected static KeyValuesEnvPostProcessor instance;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        Class<?> clazz = this.getClass();
        ReflectionUtils.doWithLocalFields(clazz, declaredField -> {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                return;
            }
            Object value = determineValue(environment, declaredField);
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
        instance = this;
    }

    protected Object determineValue(Environment environment, Field declaredField) {
        Set<String> activeProfiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
        Set<Value> valueAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(declaredField, Value.class);
        if (!valueAnnotations.isEmpty()) {
            for (Value valueAnnotation : valueAnnotations) {
                if (determineEnv(environment, activeProfiles, valueAnnotation, declaredField)) {
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

    protected boolean determineEnv(Environment environment, Set<String> activeProfiles, Value valueAnnotation, Field declaredField) {
        String profile = valueAnnotation.profile();
        return StringUtils.isBlank(profile) || activeProfiles.contains(profile);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
