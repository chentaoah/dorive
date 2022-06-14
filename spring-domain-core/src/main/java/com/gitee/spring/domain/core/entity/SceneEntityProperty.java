package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SceneEntityProperty implements EntityProperty {

    private Set<String> sceneAttribute;
    private EntityPropertyChain entityPropertyChain;

    @Override
    public Object getValue(Object entity) {
        return entityPropertyChain.getValue(entity);
    }

    @Override
    public void setValue(Object entity, Object property) {
        entityPropertyChain.setValue(entity, property);
    }

}
