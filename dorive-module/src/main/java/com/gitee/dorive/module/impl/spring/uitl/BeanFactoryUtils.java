package com.gitee.dorive.module.impl.spring.uitl;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

public class BeanFactoryUtils {

    public static Class<?> tryGetConfigurationClass(DefaultListableBeanFactory beanFactory, Class<?> beanType, Object bean) {
        // class of factory bean
        BeanDefinition beanDefinition = BeanFactoryUtils.getBeanDefinition(beanFactory, beanType, bean);
        if (beanDefinition != null && ConfigurationUtils.isConfigurationBeanDefinition(beanDefinition)) {
            AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
            String className = annotationMetadata.getClassName();
            return ClassUtil.loadClass(className);
        }
        return null;
    }

    public static BeanDefinition getBeanDefinition(DefaultListableBeanFactory beanFactory, Class<?> beanType, Object bean) {
        String[] beanNamesForType = beanFactory.getBeanNamesForType(beanType);
        if (beanNamesForType.length == 1) {
            return beanFactory.getBeanDefinition(beanNamesForType[0]);
        }
        for (String beanName : beanNamesForType) {
            Object candidateBean = beanFactory.getBean(beanName);
            if (bean == candidateBean) {
                return beanFactory.getBeanDefinition(beanName);
            }
        }
        return null;
    }

}
