package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.api.EntityFactory;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private ElementDefinition elementDefinition;

    @Override
    public Object reconstitute(BoundedContext boundedContext, Object persistentObject) {
        return BeanUtil.copyProperties(persistentObject, elementDefinition.getGenericEntityClass());
    }

}
