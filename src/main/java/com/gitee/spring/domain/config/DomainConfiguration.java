package com.gitee.spring.domain.config;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.entity.DomainConfig;
import com.gitee.spring.domain.processor.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.processor.LimitedRootInitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Order(-100)
@Configuration
public class DomainConfiguration {

    @Bean
    @ConditionalOnProperty("spring.domains")
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(Environment environment) {
        Map<String, String> domainPatternMapping = Binder.get(environment)
                .bind("spring.domains", Bindable.mapOf(String.class, String.class)).get();
        List<DomainConfig> domainConfigs = new ArrayList<>();
        domainPatternMapping.forEach((domain, pattern) ->
                StrUtil.splitTrim(pattern, ",").forEach(eachPattern ->
                        domainConfigs.add(new DomainConfig(domain, eachPattern))));
        domainConfigs.sort((o1, o2) -> o2.getDomain().compareTo(o1.getDomain()));
        return new LimitedAutowiredBeanPostProcessor(domainConfigs);
    }

    @Bean
    @ConditionalOnProperty("spring.domain.root.exclude")
    public LimitedRootInitializingBean limitedRootInitializingBean(Environment environment) {
        String rootExclude = environment.getProperty("spring.domain.root.exclude");
        return new LimitedRootInitializingBean(rootExclude);
    }

}
