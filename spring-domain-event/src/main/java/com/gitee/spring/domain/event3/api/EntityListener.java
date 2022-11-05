package com.gitee.spring.domain.event3.api;

import com.gitee.spring.domain.event3.listener.RepositoryEvent;

public interface EntityListener {

    void onApplicationEvent(RepositoryEvent repositoryEvent);

}
