package com.gitee.spring.domain.web.config;

import com.gitee.spring.domain.web.controller.RepositoryController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainWebConfiguration {

    @Bean
    public RepositoryController repositoryController() {
        return new RepositoryController();
    }

}
