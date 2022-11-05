package com.gitee.spring.domain.event.api;

import com.gitee.spring.domain.event.listener.RepositoryEvent;

public interface EntityListener {

    void onApplicationEvent(RepositoryEvent repositoryEvent);

}
