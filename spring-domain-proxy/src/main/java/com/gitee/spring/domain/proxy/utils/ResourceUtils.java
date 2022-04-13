package com.gitee.spring.domain.proxy.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {

    public static final String RESOURCE_PATTERN = "/**/*.class";

    public static List<Class<?>> resolveClasses(String basePackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;
        Resource[] resources = resourcePatternResolver.getResources(pattern);
        for (Resource resource : resources) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            String classname = metadataReader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(classname);
            classes.add(clazz);
        }
        return classes;
    }

}
