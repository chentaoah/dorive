package com.gitee.spring.domain.api;

import com.gitee.spring.domain.entity.DomainDefinition;

public interface TypeDomainResolver {

    boolean isUnderScanPackage(Class<?> typeToMatch);

    DomainDefinition resolveDomain(Class<?> typeToMatch);

    void checkDomain(Class<?> targetType, Class<?> injectedType);

    void checkDomainRoot(Class<?> targetType);

}
