package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.repository.api.RepositoryItem;

import java.util.List;

public interface Selector {

    boolean matches(RepositoryItem repositoryItem);

    List<String> select(RepositoryItem repositoryItem);

}
