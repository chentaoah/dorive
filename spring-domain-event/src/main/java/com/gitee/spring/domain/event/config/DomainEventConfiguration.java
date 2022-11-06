package com.gitee.spring.domain.event.config;

import com.gitee.spring.domain.event.listener.RepositoryListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainEventConfiguration {

    @Bean
    public RepositoryListener repositoryListener() {
        return new RepositoryListener();
    }

}
