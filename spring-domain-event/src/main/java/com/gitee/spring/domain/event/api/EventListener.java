package com.gitee.spring.domain.event.api;

import com.gitee.spring.domain.event.entity.RepositoryEvent;

public interface EventListener {

    void onApplicationEvent(RepositoryEvent repositoryEvent);

}