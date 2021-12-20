package com.gitee.spring.domain.processor;

import com.gitee.spring.domain.annotation.Root;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LimitedAutowiredBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor {

    protected final Log logger = LogFactory.getLog(getClass());
    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);
    private final Map<String, String> domainPatternMapping;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");

    @SuppressWarnings("unchecked")
    public LimitedAutowiredBeanPostProcessor(Map<String, String> domainPatternMapping) {
        this.domainPatternMapping = domainPatternMapping;
        this.autowiredAnnotationTypes.add(Autowired.class);
        try {
            this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
                    ClassUtils.forName("javax.inject.Inject", LimitedAutowiredBeanPostProcessor.class.getClassLoader()));
            logger.trace("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - simply skip.
        }
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        checkAnnotatedFieldDependency(beanType);
    }

    @Override
    public void resetBeanDefinition(String beanName) {
        // ignore
    }

    private void checkAnnotatedFieldDependency(final Class<?> clazz) {
        ReflectionUtils.doWithLocalFields(clazz, field -> {
            AnnotationAttributes ann = findAutowiredAnnotation(field);
            if (ann != null) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Autowired annotation is not supported on static fields: " + field);
                    }
                    return;
                }
                doCheckAnnotatedFieldDependency(clazz, field);
            }
        });
    }

    @Nullable
    private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
        if (ao.getAnnotations().length > 0) {  // autowiring annotations have to be local
            for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ao, type);
                if (attributes != null) {
                    return attributes;
                }
            }
        }
        return null;
    }

    private void doCheckAnnotatedFieldDependency(Class<?> clazz, Field field) {
        if (field.getType().isAnnotationPresent(Root.class)) {
            return;
        }
        String typeName = clazz.getName();
        String typeDomain = findMatchedDomain(typeName);

        String fieldTypeName = field.getType().getName();
        String fieldTypeDomain = findMatchedDomain(fieldTypeName);

        if (!Objects.equals(typeDomain, fieldTypeDomain)) {
            String message = String.format("Injection of autowired dependencies failed for class [%s]. type: [%s], typeDomain: [%s], fieldTypeName: [%s], fieldTypeDomain: [%s]",
                    clazz, typeName, typeDomain, fieldTypeName, fieldTypeDomain);
            throw new BeanCreationException(message);
        }
    }

    private String findMatchedDomain(String typeName) {
        for (Map.Entry<String, String> entry : domainPatternMapping.entrySet()) {
            if (antPathMatcher.match(entry.getValue(), typeName)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
