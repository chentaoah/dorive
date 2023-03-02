package com.gitee.dorive.api.entity.element;

import com.gitee.dorive.api.entity.def.EntityDef;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.AnnotatedElement;

@Data
@NoArgsConstructor
public class EntityEle {

    private AnnotatedElement element;
    private EntityDef entityDef;

    public EntityEle(AnnotatedElement element) {
        this.element = element;
        this.entityDef = EntityDef.fromElement(element);
    }

}
