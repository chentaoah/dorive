package com.gitee.dorive.api.entity.element;

import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.EntityDef;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.AnnotatedElement;

@Data
@NoArgsConstructor
public abstract class EntityEle {

    private AnnotatedElement element;
    private EntityDef entityDef;
    private PropProxy pkProxy;

    public EntityEle(AnnotatedElement element) {
        this.element = element;
        this.entityDef = EntityDef.fromElement(element);
    }

    public boolean isEntity() {
        return entityDef != null;
    }

    public boolean isAggregated() {
        return entityDef != null && entityDef.getRepository() != Object.class;
    }
    
    public void initialize() {
        if (entityDef != null && pkProxy == null) {
            doInitialize();
        }
    }

    protected abstract boolean isCollection();

    protected abstract Class<?> getGenericType();

    protected abstract EntityType getEntityType();

    protected abstract void doInitialize();

}
