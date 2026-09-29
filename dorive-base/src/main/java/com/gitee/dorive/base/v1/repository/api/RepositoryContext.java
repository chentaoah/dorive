/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.repository.api;

import com.gitee.dorive.base.v1.definition.def.RepositoryDef;
import com.gitee.dorive.base.v1.event.api.EventPublisher;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public interface RepositoryContext extends RepositoryEle {

    ApplicationContext getApplicationContext();

    RepositoryDef getRepositoryDef();

    Map<String, RepositoryItem> getRepositoryMap();

    RepositoryItem getRootRepository();

    List<RepositoryItem> getSubRepositories();

    List<RepositoryItem> getOrderedRepositories();

    boolean matches(Options options, Operation operation, RepositoryItem repositoryItem);

    EventPublisher getExecutorEventPublisher();

    EventPublisher getRepositoryEventPublisher();

}
