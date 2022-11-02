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

    protected boolean aggregated;
    protected boolean aggregateRoot;
    protected String accessPath;
    protected BinderResolver binderResolver;
    protected boolean boundEntity;
    protected PropertyChain anchorPoint;
    protected String fieldPrefix;
    protected Map<String, PropertyChain> propertyChainMap;

    public boolean matchContext(BoundedContext boundedContext) {
        String[] triggers = entityDefinition.getTriggers();
        if (triggers == null || triggers.length == 0) {
            return true;
        }
        for (String trigger : triggers) {
            if (boundedContext.containsKey(trigger)) {
                return true;
            }
        }
        return false;
    }

}
