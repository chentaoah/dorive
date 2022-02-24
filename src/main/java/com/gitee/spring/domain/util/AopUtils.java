package com.gitee.spring.domain.util;

import java.lang.annotation.Annotation;

public class AopUtils {

    public static String getAnnotatedClassName(Object instance, Class<? extends Annotation> annotationType) {
        Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(instance);
        if (targetClass.isAnnotationPresent(annotationType)) {
            return targetClass.getName();
        } else {
            Class<?> superClass = targetClass.getSuperclass();
            if (superClass.isAnnotationPresent(annotationType)) {
                return superClass.getName();
            }
            Class<?>[] interfaceClasses = targetClass.getInterfaces();
            for (Class<?> interfaceClass : interfaceClasses) {
                if (interfaceClass.isAnnotationPresent(annotationType)) {
                    return interfaceClass.getName();
                }
            }
        }
        return targetClass.getName();
    }

}
