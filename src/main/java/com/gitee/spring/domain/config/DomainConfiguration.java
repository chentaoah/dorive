package com.gitee.spring.domain.config;

import com.gitee.spring.domain.config.entity.DomainConfig;
import com.gitee.spring.domain.processor.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.processor.LimitedRootInitializingBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Order(-100)
@Configuration
public class DomainConfiguration {

    @Bean
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(Environment environment) {
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        List<DomainConfig> domainConfigs = new ArrayList<>();
        domainPatternMapping.forEach((domain, pattern) -> domainConfigs.add(new DomainConfig(domain, pattern)));
        domainConfigs.sort((o1, o2) -> o2.getDomain().compareTo(o1.getDomain()));
        return new LimitedAutowiredBeanPostProcessor(domainConfigs);
    }

    @Bean
    public LimitedRootInitializingBean limitedRootInitializingBean(Environment environment) {
        String sign = environment.getProperty("spring.domain.sign");
        if (StringUtils.isBlank(sign)) {
            throw new BeanCreationException("Missing configuration! name: [spring.domain.sign]");
        }
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        return new LimitedRootInitializingBean(domainPatternMapping, sign);
    }

}
