package com.gitee.spring.domain.event.api;

import com.gitee.spring.domain.event.entity.RepositoryEvent;

public interface EntityListener {

    void onApplicationEvent(RepositoryEvent repositoryEvent);

}
