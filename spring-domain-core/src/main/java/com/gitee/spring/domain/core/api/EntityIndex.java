package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.ForeignKey;

import java.util.List;

public interface EntityIndex {

    List<Object> selectList(Object rootEntity, ForeignKey foreignKey);

}
