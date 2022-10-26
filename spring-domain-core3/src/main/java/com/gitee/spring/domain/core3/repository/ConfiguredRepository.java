package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core3.entity.BoundedContext;
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

    public boolean matchContext(BoundedContext boundedContext) {
        String[] context = entityDefinition.getContext();
        if (context == null || context.length == 0) {
            return true;
        }
        for (String eachContext : context) {
            if (boundedContext.containsKey(eachContext)) {
                return true;
            }
        }
        return false;
    }

}
