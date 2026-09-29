/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.launcher.v1.configuration;

import com.gitee.dorive.definition.v1.impl.DefaultEntityTypeResolver;
import com.gitee.dorive.definition.v1.impl.DefaultQueryTypeResolver;
import com.gitee.dorive.base.v1.definition.api.EntityTypeResolver;
import com.gitee.dorive.base.v1.definition.api.QueryTypeResolver;
import com.gitee.dorive.launcher.v1.impl.builder.DefaultRepositoryContextBuilder;
import com.gitee.dorive.repository.v1.api.RepositoryContextBuilder;
import com.gitee.dorive.repository.v1.impl.context.RepositoryRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DoriveCoreConfiguration {

    @Bean("EntityTypeResolverV3")
    public static EntityTypeResolver entityResolver() {
        return new DefaultEntityTypeResolver();
    }

    @Bean("QueryTypeResolverV3")
    public static QueryTypeResolver queryResolver() {
        return new DefaultQueryTypeResolver();
    }

    @Bean("RepositoryRegisterV3")
    public static RepositoryRegister repositoryGlobalContext() {
        return new RepositoryRegister();
    }

    @Bean("RepositoryBuilderV3")
    public static RepositoryContextBuilder repositoryBuilder() {
        return new DefaultRepositoryContextBuilder();
    }

}
