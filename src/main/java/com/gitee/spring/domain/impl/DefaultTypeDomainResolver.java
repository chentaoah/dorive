package com.gitee.spring.domain.impl;

import cn.hutool.core.collection.CollUtil;
import com.gitee.spring.domain.annotation.Root;
import com.gitee.spring.domain.api.TypeDomainResolver;
import com.gitee.spring.domain.entity.DomainDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Objects;

public class DefaultTypeDomainResolver implements TypeDomainResolver {

    private final String scanPackage;
    private final List<DomainDefinition> domainDefinitions;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");

    public DefaultTypeDomainResolver(String scanPackage, List<DomainDefinition> domainDefinitions) {
        this.scanPackage = scanPackage;
        this.domainDefinitions = domainDefinitions;
    }

    @Override
    public boolean isUnderScanPackage(Class<?> typeToMatch) {
        return antPathMatcher.match(scanPackage, typeToMatch.getName());
    }

    @Override
    public DomainDefinition resolveDomain(Class<?> typeToMatch) {
        return CollUtil.findOne(domainDefinitions, item -> antPathMatcher.match(item.getPattern(), typeToMatch.getName()));
    }

    @Override
    public void checkDomain(Class<?> targetType, Class<?> injectedType) {
        if (injectedType.isAnnotationPresent(Root.class)) {
            return;
        }
        DomainDefinition injectedDomainDefinition = resolveDomain(injectedType);
        if (injectedDomainDefinition == null) {
            return;
        }
        DomainDefinition targetDomainDefinition = resolveDomain(targetType);
        if (targetDomainDefinition != null) {
            boolean isMatch = Objects.equals(targetDomainDefinition.getName(), injectedDomainDefinition.getName())
                    || targetDomainDefinition.getName().startsWith(injectedDomainDefinition.getName() + "-");
            if (!isMatch) {
                throwInjectionException(targetType, targetDomainDefinition, injectedType, injectedDomainDefinition);
            }
        } else {
            throwInjectionException(targetType, null, injectedType, injectedDomainDefinition);
        }
    }

    protected void throwInjectionException(Class<?> targetType, DomainDefinition targetDomainDefinition,
                                           Class<?> injectedType, DomainDefinition injectedDomainDefinition) {
        String message = String.format("Injection of autowired dependencies failed! targetType: [%s], targetDomain: [%s], injectedType: [%s], injectedDomain: [%s]",
                targetType.getName(), targetDomainDefinition != null ? targetDomainDefinition.getName() : null,
                injectedType.getName(), injectedDomainDefinition.getName());
        throw new BeanCreationException(message);
    }

    @Override
    public void checkDomainRoot(Class<?> targetType) {
        DomainDefinition domainDefinition = resolveDomain(targetType);
        if (domainDefinition != null && StringUtils.isNotBlank(domainDefinition.getProtect())) {
            if (antPathMatcher.match(domainDefinition.getProtect(), targetType.getName())) {
                String message = String.format("The type cannot be annotated by @Root! protect: [%s], targetType: [%s]",
                        domainDefinition.getProtect(), targetType.getName());
                throw new BeanCreationException(message);
            }
        }
    }

}
