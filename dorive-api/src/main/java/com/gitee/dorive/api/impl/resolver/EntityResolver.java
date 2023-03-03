package com.gitee.dorive.api.impl.resolver;

import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.api.util.PathUtils;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class EntityResolver {

    private EntityType entityType;
    private Map<String, EntityEle> entityEleMap = new LinkedHashMap<>();

    public void resolve(Class<?> type) {
        entityType = EntityType.getInstance(type);
        entityEleMap.put("/", entityType);
        resolve("", entityType.getEntityFields().values());
    }

    private void resolve(String lastAccessPath, Collection<EntityField> entityFields) {
        for (EntityField entityField : entityFields) {
            String accessPath = lastAccessPath + "/" + entityField.getName();
            renewDef(accessPath, entityField);
            entityEleMap.put(accessPath, entityField);
            EntityType entityType = entityField.getEntityType();
            if (entityType != null) {
                resolve(accessPath, entityType.getEntityFields().values());
            }
        }
    }

    private void renewDef(String accessPath, EntityField entityField) {
        List<BindingDef> bindingDefs = entityField.getBindingDefs();
        for (BindingDef bindingDef : bindingDefs) {
            String bindExp = bindingDef.getBindExp();
            if (bindExp.startsWith(".")) {
                bindExp = PathUtils.getAbsolutePath(accessPath, bindExp);
                bindingDef.setBindExp(bindExp);
            }
        }
    }

}
