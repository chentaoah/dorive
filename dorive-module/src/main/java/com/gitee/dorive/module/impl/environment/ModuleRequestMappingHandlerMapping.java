package com.gitee.dorive.module.impl.environment;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.util.PlaceholderUtils;
import com.gitee.dorive.module.impl.util.SpringClassUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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
        if (requestMapping instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(requestMapping);
            if (SpringClassUtils.isSynthesizedMergedAnnotationInvocationHandler(invocationHandler)) {
                MergedAnnotation<?> annotation = (MergedAnnotation<?>) ReflectUtil.getFieldValue(invocationHandler, "annotation");
                Object source = annotation.getSource();
                if (source instanceof Class<?>) {
                    Class<?> sourceType = (Class<?>) source;
                    if (moduleParser.isUnderScanPackage(sourceType.getName())) {
                        ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(sourceType);
                        String name = moduleDefinition.getName();
                        String version = moduleDefinition.getVersion();
                        // 替换占位符
                        List<String> pathList = new ArrayList<>(paths.length);
                        for (String path : paths) {
                            if (PlaceholderUtils.contains(path)) {
                                path = PlaceholderUtils.replace(path, str -> "$$P$$" + name + "." + version + "." + str + "$$S$$");
                                path = StrUtil.replace(path, "$$P$$", "${");
                                path = StrUtil.replace(path, "$$S$$", "}");
                            }
                            pathList.add(path);
                        }
                        paths = pathList.toArray(new String[0]);
                    }
                }
            }
        }
        return paths;
    }

}
