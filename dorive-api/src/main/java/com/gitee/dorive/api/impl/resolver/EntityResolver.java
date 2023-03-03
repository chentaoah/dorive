package com.gitee.dorive.api.impl.resolver;

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EntityResolver {

    private EntityType entityType;
    private Map<String, EntityEle> entityEleMap = new LinkedHashMap<>();

    public EntityResolver(Class<?> type) {
        this.entityType = EntityType.getInstance(type);
        this.entityEleMap.put("/", this.entityType);
        resolve("", this.entityType.getEntityFields().values());
    }

    private void resolve(String lastAccessPath, Collection<EntityField> entityFields) {
        for (EntityField entityField : entityFields) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            entityEleMap.put(accessPath, entityField);
            EntityType entityType = entityField.getEntityType();
            if (entityType != null) {
                resolve(accessPath, entityType.getEntityFields().values());
            }
        }
    }

}
