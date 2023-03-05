package com.gitee.dorive.api.impl.resolver;

import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.api.entity.element.PropChain;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropChainResolver {

    private Map<String, PropChain> propChainMap = new LinkedHashMap<>();

    public PropChainResolver(EntityType entityType) {
        resolve("", entityType);
    }

    public PropChainResolver(String lastAccessPath, EntityType entityType) {
        resolve(lastAccessPath, entityType);
    }

    private void resolve(String lastAccessPath, EntityType entityType) {
        PropChain lastPropChain = propChainMap.get(lastAccessPath);
        for (EntityField entityField : entityType.getEntityFields().values()) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            PropChain propChain = new PropChain(lastPropChain, entityType, accessPath, entityField);
            propChainMap.put(accessPath, propChain);
            if (EntityField.filter(entityField.getType()) && !entityField.isAnnotatedEntity()) {
                resolve(accessPath, entityField.getEntityType());
            }
        }
    }

}
