package com.gitee.spring.domain.injection.api;

import com.gitee.spring.domain.injection.entity.DomainDefinition;

public interface TypeDomainResolver {

    boolean isUnderScanPackage(Class<?> typeToMatch);

    DomainDefinition resolveDomain(Class<?> typeToMatch);

    void checkDomain(Class<?> targetType, Class<?> injectedType);

    void checkDomainRoot(Class<?> targetType);

}
