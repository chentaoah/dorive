package com.gitee.spring.domain.proxy.extension;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.api.ProxyCompiler;
import com.gitee.spring.domain.proxy.compile.JavassistCompiler;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityPropertyFactory {

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final ProxyCompiler PROXY_COMPILER = new JavassistCompiler();
    private static final Map<String, EntityProperty> GENERATED_PROXY_CACHE = new LinkedHashMap<>();

    public static EntityProperty newEntityProperty(Class<?> lastEntityClass, Class<?> entityClass, String fieldName) {
        String cacheKey = lastEntityClass.getTypeName() + ":" + entityClass.getTypeName() + ":" + fieldName;
        if (GENERATED_PROXY_CACHE.containsKey(cacheKey)) {
            return GENERATED_PROXY_CACHE.get(cacheKey);
        }
        try {
            String generatedCode = generateCode(lastEntityClass, entityClass, fieldName);
            Class<?> generatedClass = PROXY_COMPILER.compile(generatedCode, null);
            EntityProperty entityProperty = (EntityProperty) ReflectUtils.newInstance(generatedClass);
            GENERATED_PROXY_CACHE.put(cacheKey, entityProperty);
            return entityProperty;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate class!", e);
        }
    }

    private static String generateCode(Class<?> lastEntityClass, Class<?> entityClass, String fieldName) {
        Class<?> interfaceClass = EntityProperty.class;
        StringBuilder builder = new StringBuilder();
        String simpleName = interfaceClass.getSimpleName() + "$Proxy" + COUNT.getAndIncrement();
        builder.append(String.format("package %s;\n", interfaceClass.getPackage().getName()));
        builder.append(String.format("public class %s implements %s {\n", simpleName, interfaceClass.getName()));

        builder.append("\t").append(String.format("public %s getValue(%s arg0) {\n", Object.class.getTypeName(), Object.class.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg1 = (%s)arg0;\n", lastEntityClass.getTypeName(), lastEntityClass.getTypeName()));
        builder.append("\t\t").append(String.format("return arg1.get%s();\n", StrUtil.upperFirst(fieldName)));
        builder.append("\t").append("}\n");

        builder.append("\t").append(String.format("public void setValue(%s arg0, %s arg1) {\n", Object.class.getTypeName(), Object.class.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg2 = (%s)arg0;\n", lastEntityClass.getTypeName(), lastEntityClass.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg3 = (%s)arg1;\n", entityClass.getTypeName(), entityClass.getTypeName()));
        builder.append("\t\t").append(String.format("arg2.set%s(arg3);\n", StrUtil.upperFirst(fieldName)));
        builder.append("\t").append("}\n");

        builder.append("}\n");
        return builder.toString();
    }

}
