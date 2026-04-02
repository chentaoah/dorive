/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.launcher.v1.configuration;

import com.gitee.dorive.definition.v1.impl.DefaultEntityTypeResolver;
import com.gitee.dorive.definition.v1.impl.DefaultQueryTypeResolver;
import com.gitee.dorive.base.v1.definition.api.EntityTypeResolver;
import com.gitee.dorive.base.v1.definition.api.QueryTypeResolver;
import com.gitee.dorive.launcher.v1.impl.builder.DefaultRepositoryBuilder;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
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
    public static RepositoryBuilder repositoryBuilder() {
        return new DefaultRepositoryBuilder();
    }

}
