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
import com.gitee.dorive.env.util.AopUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

@Data
public class KeyValuesResolver {

    private Environment environment;
    private Set<String> activeProfiles;

    public KeyValuesResolver(Environment environment) {
        this.environment = environment;
        this.activeProfiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
    }

    public Properties resolveProperties(Object instance) {
        Properties properties = new Properties();
        Class<?> targetClass = AopUtils.getTargetClass(instance);
        ReflectionUtils.doWithLocalFields(targetClass, declaredField -> {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                return;
            }
            Object value = determineValue(declaredField);
            if (value != null) {
                ReflectUtil.setFieldValue(instance, declaredField, value);
                Key keyAnnotation = declaredField.getAnnotation(Key.class);
                if (keyAnnotation != null) {
                    String property = keyAnnotation.value();
                    if (StringUtils.isNotBlank(property) && !environment.containsProperty(property)) {
                        properties.setProperty(property, value.toString());
                    }
                }
            }
        });
        return properties;
    }

    protected Object determineValue(Field declaredField) {
        Set<Value> valueAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(declaredField, Value.class);
        if (!valueAnnotations.isEmpty()) {
            for (Value valueAnnotation : valueAnnotations) {
                if (matchEnvironment(valueAnnotation, declaredField)) {
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

    protected boolean matchEnvironment(Value valueAnnotation, Field declaredField) {
        String profile = valueAnnotation.profile();
        return StringUtils.isBlank(profile) || activeProfiles.contains(profile);
    }

}
