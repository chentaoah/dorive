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

package com.gitee.dorive.module.impl.environment;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.util.PlaceholderUtils;
import com.gitee.dorive.module.impl.util.SpringClassUtils;
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

@Getter
@Setter
public class ModuleRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

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
        if (paths.length == 1 && !paths[0].contains("$")) {
            return paths;
        }
        if (requestMapping instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(requestMapping);
            if (SpringClassUtils.isSynthesizedMergedAnnotationInvocationHandler(invocationHandler)) {
                MergedAnnotation<?> annotation = (MergedAnnotation<?>) ReflectUtil.getFieldValue(invocationHandler, "annotation");
                Object source = annotation.getSource();
                Class<?> sourceType = null;
                if (source instanceof Class<?>) {
                    sourceType = (Class<?>) source;

                } else if (source instanceof Method) {
                    sourceType = ((Method) source).getDeclaringClass();
                }
                if (sourceType != null && moduleParser.isUnderScanPackage(sourceType.getName())) {
                    ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(sourceType);
                    // 替换占位符
                    List<String> pathList = new ArrayList<>(paths.length);
                    for (String path : paths) {
                        if (PlaceholderUtils.contains(path)) {
                            path = PlaceholderUtils.replace(path, strValue -> "$$P$$" + moduleDefinition.getPropertiesPrefix() + strValue + "$$S$$");
                            path = StrUtil.replace(path, "$$P$$", "${");
                            path = StrUtil.replace(path, "$$S$$", "}");
                        }
                        pathList.add(path);
                    }
                    paths = pathList.toArray(new String[0]);
                }
            }
        }
        return paths;
    }

}
