package com.gitee.spring.domain.core.api;

import java.util.List;

public interface EntityIndex {
    
    List<Object> selectList(Object rootEntity);

}
