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

package com.gitee.dorive.autoconfigure.core;

import com.gitee.dorive.aggregate.v1.impl.DefaultEntityResolver;
import com.gitee.dorive.aggregate.v1.impl.DefaultQueryResolver;
import com.gitee.dorive.base.v1.aggregate.api.EntityResolver;
import com.gitee.dorive.base.v1.aggregate.api.QueryResolver;
import com.gitee.dorive.base.v1.executor.api.EntityHandlerFactory;
import com.gitee.dorive.base.v1.executor.api.EntityJoinerFactory;
import com.gitee.dorive.core.impl.factory.DefaultEntityHandlerFactory;
import com.gitee.dorive.core.impl.factory.DefaultEntityJoinerFactory;
import com.gitee.dorive.core.impl.factory.DefaultRepositoryBuilder;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.impl.context.RepositoryGlobalContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DoriveCoreConfiguration {

    @Bean("EntityResolverV3")
    public static EntityResolver entityResolver() {
        return new DefaultEntityResolver();
    }

    @Bean("QueryResolverV3")
    public static QueryResolver queryResolver() {
        return new DefaultQueryResolver();
    }

    @Bean("RepositoryGlobalContextV3")
    public static RepositoryGlobalContext repositoryGlobalContext() {
        return new RepositoryGlobalContext();
    }

    @Bean("RepositoryBuilderV3")
    public static RepositoryBuilder repositoryBuilder() {
        return new DefaultRepositoryBuilder();
    }

    @Bean("EntityHandlerFactoryV3")
    public static EntityHandlerFactory entityHandlerFactory() {
        return new DefaultEntityHandlerFactory();
    }

    @Bean("EntityJoinerFactoryV3")
    public static EntityJoinerFactory entityJoinerFactory() {
        return new DefaultEntityJoinerFactory();
    }

}
