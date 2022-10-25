package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.impl.BinderResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends ProxyRepository {
    protected boolean aggregateRoot;
    protected String accessPath;
    protected BinderResolver binderResolver;
    protected boolean boundEntity;
    protected EntityPropertyChain anchorPoint;
    protected Map<String, EntityPropertyChain> properties;
}
