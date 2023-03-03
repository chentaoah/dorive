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

    public boolean isAggregated() {
        return entityDef.getRepository() != Object.class;
    }

    public boolean isCollection() {
        return false;
    }

    public Class<?> getGenericType() {
        return null;
    }

    public void initialize() {
        if (entityDef != null && pkProxy == null) {
            doInitialize();
        }
    }

    protected abstract void doInitialize();

}
