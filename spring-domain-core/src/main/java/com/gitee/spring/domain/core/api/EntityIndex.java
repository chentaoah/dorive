package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.List;

public interface EntityIndex {

    String buildForeignKey(ConfiguredRepository configuredRepository, Object rootEntity);

    List<Object> selectList(String foreignKey);

}
