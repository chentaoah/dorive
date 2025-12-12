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

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.entity.ModuleDefinition;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.v1.impl.util.PlaceholderUtils;
import com.gitee.dorive.module.v1.impl.util.SpringClassUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ModuleRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private Map<Class<?>, String[]> classRequestMappingPathsCache = new ConcurrentHashMap<>();

    @Override
    protected RequestMappingInfo createRequestMappingInfo(
            RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {
        String[] paths = requestMapping.path();
        paths = handlePaths(requestMapping, paths);
        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(paths))
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces())
                .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(getBuilderConfiguration()).build();
    }

    private String[] handlePaths(RequestMapping requestMapping, String[] paths) {
        if (paths == null || paths.length == 0) {
            return paths;
        }
        if (paths.length == 1 && !paths[0].contains("$")) {
            return paths;
        }
        if (requestMapping instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(requestMapping);
            if (SpringClassUtils.isSynthesizedMergedAnnotationInvocationHandler(invocationHandler)) {
                MergedAnnotation<?> annotation = (MergedAnnotation<?>) ReflectUtil.getFieldValue(invocationHandler, "annotation");
                Object source = annotation.getSource();
                Class<?> clazz = null;
                boolean isTypeAnnotated = false;
                if (source instanceof Class<?>) {
                    clazz = (Class<?>) source;
                    isTypeAnnotated = true;

                } else if (source instanceof Method) {
                    clazz = ((Method) source).getDeclaringClass();
                }
                if (clazz != null && moduleParser.isUnderScanPackage(clazz.getName())) {
                    if (isTypeAnnotated) {
                        String[] existPaths = classRequestMappingPathsCache.get(clazz);
                        if (existPaths != null) {
                            return existPaths;
                        }
                    }
                    ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(clazz);
                    if (moduleDefinition != null) {
                        String[] newPaths = addPrefixForPlaceholders(moduleDefinition.getPropertiesPrefix(), paths);
                        if (isTypeAnnotated) {
                            classRequestMappingPathsCache.put(clazz, newPaths);
                        }
                        return newPaths;
                    }
                }
            }
        }
        return paths;
    }

    private String[] addPrefixForPlaceholders(String propertiesPrefix, String[] paths) {
        // 替换占位符
        List<String> pathList = new ArrayList<>(paths.length);
        for (String path : paths) {
            if (PlaceholderUtils.contains(path)) {
                path = PlaceholderUtils.replace(path, strValue -> "$$P$$" + propertiesPrefix + strValue + "$$S$$");
                path = StrUtil.replace(path, "$$P$$", "${");
                path = StrUtil.replace(path, "$$S$$", "}");
            }
            pathList.add(path);
        }
        return pathList.toArray(new String[0]);
    }

}
