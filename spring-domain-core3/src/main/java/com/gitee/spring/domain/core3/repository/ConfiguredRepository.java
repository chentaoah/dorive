package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.impl.resolver.BinderResolver;
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
    protected PropertyChain anchorPoint;
    protected String prefixAccessPath;
    protected Map<String, PropertyChain> propertyChains;
}
