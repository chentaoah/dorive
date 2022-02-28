package com.gitee.spring.domain.processor;

import cn.hutool.core.collection.CollUtil;
import com.gitee.spring.domain.annotation.Root;
import com.gitee.spring.domain.api.TypeDomainResolver;
import com.gitee.spring.domain.entity.DomainConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Objects;

public class LimitedTypeDomainResolver implements TypeDomainResolver {

    private final String scanPackage;
    private final List<DomainConfig> domainConfigs;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");

    public LimitedTypeDomainResolver(String scanPackage, List<DomainConfig> domainConfigs) {
        this.scanPackage = scanPackage;
        this.domainConfigs = domainConfigs;
    }

    @Override
    public boolean isUnderScanPackage(Class<?> typeToMatch) {
        return antPathMatcher.match(scanPackage, typeToMatch.getName());
    }

    @Override
    public DomainConfig resolveDomain(Class<?> typeToMatch) {
        return CollUtil.findOne(domainConfigs, item -> antPathMatcher.match(item.getPattern(), typeToMatch.getName()));
    }

    @Override
    public void checkDomain(Class<?> targetType, Class<?> injectedType) {
        if (injectedType.isAnnotationPresent(Root.class)) {
            return;
        }
        DomainConfig injectedDomainConfig = resolveDomain(injectedType);
        if (injectedDomainConfig == null) {
            return;
        }
        DomainConfig domainConfig = resolveDomain(targetType);
        if (domainConfig != null) {
            boolean isMatch = Objects.equals(domainConfig.getName(), injectedDomainConfig.getName())
                    || domainConfig.getName().startsWith(injectedDomainConfig.getName() + "-");
            if (!isMatch) {
                throwInjectionException(targetType, domainConfig, injectedType, injectedDomainConfig);
            }
        } else {
            throwInjectionException(targetType, null, injectedType, injectedDomainConfig);
        }
    }

    @Override
    public void checkDomainRoot(Class<?> targetType) {
        DomainConfig domainConfig = resolveDomain(targetType);
        if (domainConfig != null && StringUtils.isNotBlank(domainConfig.getProtect())) {
            if (antPathMatcher.match(domainConfig.getProtect(), targetType.getName())) {
                String message = String.format("The type cannot be annotated by @Root! protect: [%s], typeName: [%s]",
                        domainConfig.getProtect(), targetType.getName());
                throw new BeanCreationException(message);
            }
        }
    }

    private void throwInjectionException(Class<?> type, DomainConfig domainConfig, Class<?> injectedType, DomainConfig injectedDomainConfig) {
        String message = String.format("Injection of autowired dependencies failed! typeName: [%s], typeDomain: [%s], fieldTypeName: [%s], fieldTypeDomain: [%s]",
                type.getName(), domainConfig != null ? domainConfig.getName() : null, injectedType.getName(), injectedDomainConfig.getName());
        throw new BeanCreationException(message);
    }

}
