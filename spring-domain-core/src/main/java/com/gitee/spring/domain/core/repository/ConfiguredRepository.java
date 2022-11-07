package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.MetadataGetter;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends ProxyRepository implements MetadataGetter {

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

    @Override
    public Object getMetadata() {
        AbstractRepository<Object, Object> proxyRepository = getProxyRepository();
        if (proxyRepository instanceof MetadataGetter) {
            return ((MetadataGetter) proxyRepository).getMetadata();
        }
        return null;
    }

}
