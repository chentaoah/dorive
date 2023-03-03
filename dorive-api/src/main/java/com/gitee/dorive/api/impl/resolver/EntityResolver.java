package com.gitee.dorive.api.impl.resolver;

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EntityResolver {

    private Map<String, EntityEle> entityEleMap = new LinkedHashMap<>();

    public EntityResolver(EntityType entityType) {
        this.entityEleMap.put("/", entityType);
        resolve("", entityType);
    }

    private void resolve(String lastAccessPath, EntityType entityType) {
        for (EntityField entityField : entityType.getEntityFields().values()) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            entityEleMap.put(accessPath, entityField);
            EntityType fieldEntityType = entityField.getEntityType();
            if (fieldEntityType != null) {
                resolve(accessPath, fieldEntityType);
            }
        }
    }

}
