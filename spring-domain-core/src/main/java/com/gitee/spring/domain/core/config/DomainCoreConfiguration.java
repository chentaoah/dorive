package com.gitee.spring.domain.core.config;

import com.gitee.spring.domain.core.property.DefaultEntityAssembler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainCoreConfiguration {

    @Bean
    public DefaultEntityAssembler defaultEntityAssembler() {
        return new DefaultEntityAssembler();
    }

}
