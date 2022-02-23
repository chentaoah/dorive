package com.gitee.spring.domain.processor;

import cn.hutool.core.collection.CollUtil;
import com.gitee.spring.domain.annotation.Root;
import com.gitee.spring.domain.entity.DomainConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;

public class LimitedAutowiredBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor {

    protected final Log logger = LogFactory.getLog(getClass());
    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);
    private final List<DomainConfig> domainConfigs;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");

    @SuppressWarnings("unchecked")
    public LimitedAutowiredBeanPostProcessor(List<DomainConfig> domainConfigs) {
        this.domainConfigs = domainConfigs;
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
        checkAutowiredFieldDomain(beanType);
    }

    @Override
    public void resetBeanDefinition(String beanName) {
        // ignore
    }

    private void checkAutowiredFieldDomain(final Class<?> clazz) {
        ReflectionUtils.doWithLocalFields(clazz, field -> {
            AnnotationAttributes ann = findAutowiredAnnotation(field);
            if (ann != null) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Autowired annotation is not supported on static fields: " + field);
                    }
                    return;
                }
                doCheckAutowiredFieldDomain(clazz, field);
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

    private void doCheckAutowiredFieldDomain(Class<?> clazz, Field field) {
        if (field.getType().isAnnotationPresent(Root.class)) return;

        String fieldTypeName = field.getType().getName();
        String fieldTypeDomain = findDomainByPattern(fieldTypeName);

        if (StringUtils.isBlank(fieldTypeDomain)) return;

        String typeName = clazz.getName();
        String typeDomain = findDomainByPattern(typeName);

        if (StringUtils.isBlank(typeDomain)) {
            throwInjectionException(typeName, typeDomain, fieldTypeName, fieldTypeDomain);
        }

        boolean flag = Objects.equals(typeDomain, fieldTypeDomain) || typeDomain.startsWith(fieldTypeDomain + "-");
        if (!flag) {
            throwInjectionException(typeName, typeDomain, fieldTypeName, fieldTypeDomain);
        }
    }

    private String findDomainByPattern(String typeName) {
        DomainConfig domainConfig = CollUtil.findOne(domainConfigs, item -> antPathMatcher.match(item.getPattern(), typeName));
        return domainConfig != null ? domainConfig.getName() : null;
    }

    private void throwInjectionException(String typeName, String typeDomain, String fieldTypeName, String fieldTypeDomain) {
        String message = String.format("Injection of autowired dependencies failed! typeName: [%s], typeDomain: [%s], fieldTypeName: [%s], fieldTypeDomain: [%s]",
                typeName, typeDomain, fieldTypeName, fieldTypeDomain);
        throw new BeanCreationException(message);
    }

}
