package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.EntityExample;

public interface EntityCriterion {
    
    void appendTo(EntityExample entityExample);

}
