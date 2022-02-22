package com.gitee.spring.domain.processor;

import com.gitee.spring.domain.annotation.Root;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;

import java.util.Map;

public class LimitedRootInitializingBean implements ApplicationContextAware, InitializingBean {

    private final String rootExclude;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private ApplicationContext applicationContext;

    public LimitedRootInitializingBean(String rootExclude) {
        this.rootExclude = rootExclude;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Root.class);
        beans.forEach((id, bean) -> {
            String className = bean.getClass().getName();
            if (antPathMatcher.match(rootExclude, className)) {
                String message = String.format("The type cannot be annotated by @Root! exclude: [%s], typeName: [%s]", rootExclude, className);
                throw new BeanCreationException(message);
            }
        });
    }

}
