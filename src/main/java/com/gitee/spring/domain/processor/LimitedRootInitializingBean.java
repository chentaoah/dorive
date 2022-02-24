package com.gitee.spring.domain.processor;

import cn.hutool.core.collection.CollUtil;
import com.gitee.spring.domain.annotation.Root;
import com.gitee.spring.domain.entity.DomainConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

public class LimitedRootInitializingBean implements ApplicationContextAware, InitializingBean {

    private final List<DomainConfig> domainConfigs;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private ApplicationContext applicationContext;

    public LimitedRootInitializingBean(List<DomainConfig> domainConfigs) {
        this.domainConfigs = domainConfigs;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Root.class);
        beans.forEach((id, bean) -> {
            String className = AopUtils.getTargetClass(bean).getName();
            DomainConfig domainConfig = findDomainByPattern(className);
            if (domainConfig != null && StringUtils.isNotBlank(domainConfig.getProtect())) {
                if (antPathMatcher.match(domainConfig.getProtect(), className)) {
                    String message = String.format("The type cannot be annotated by @Root! protect: [%s], typeName: [%s]", domainConfig.getProtect(), className);
                    throw new BeanCreationException(message);
                }
            }
        });
    }

    private DomainConfig findDomainByPattern(String typeName) {
        return CollUtil.findOne(domainConfigs, item -> antPathMatcher.match(item.getPattern(), typeName));
    }

}
