package com.gitee.spring.domain.config;

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

import java.util.Map;

@Order(-100)
@Configuration
public class DomainConfiguration {

    @Bean
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(Environment environment) {
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        return new LimitedAutowiredBeanPostProcessor(domainPatternMapping);
    }

    @Bean
    public LimitedRootInitializingBean limitedRootInitializingBean(Environment environment) {
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        String sign = environment.getProperty("spring.domain.sign");
        if (StringUtils.isBlank(sign)) {
            throw new BeanCreationException("Missing configuration! name: [spring.domain.sign]");
        }
        return new LimitedRootInitializingBean(domainPatternMapping, sign);
    }

}
