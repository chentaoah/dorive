package com.gitee.spring.domain.api;

import com.gitee.spring.domain.entity.DomainConfig;

public interface TypeDomainResolver {

    DomainConfig resolveDomain(Class<?> typeToMatch);

    void checkDomain(Class<?> targetType, Class<?> injectedType);

    void checkDomainRoot(Class<?> targetType);

}
