package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.List;

public interface EntityIndex {

    List<Object> selectList(Object rootEntity, ConfiguredRepository configuredRepository);

}
