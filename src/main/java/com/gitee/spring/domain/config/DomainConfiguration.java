package com.gitee.spring.domain.config;

import com.gitee.spring.domain.processor.LimitedAnnotationBeanPostProcessor;
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
    @ConditionalOnMissingBean(LimitedAnnotationBeanPostProcessor.class)
    public LimitedAnnotationBeanPostProcessor limitedAnnotationBeanPostProcessor(Environment environment) {
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        return new LimitedAnnotationBeanPostProcessor(domainPatternMapping);
    }

}
