package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultPropertyConverter implements PropertyConverter {

    protected BindingDefinition bindingDefinition;

    @Override
    public Object convert(BoundedContext boundedContext, Object property) {
        String propertyAttribute = bindingDefinition.getPropertyAttribute();
        if (StringUtils.isBlank(propertyAttribute)) {
            return property;
        } else {
            if (property instanceof Collection) {
                List<Object> fieldValues = new ArrayList<>();
                for (Object eachProperty : (Collection<?>) property) {
                    Object fieldValue = BeanUtil.getFieldValue(eachProperty, propertyAttribute);
                    fieldValues.add(fieldValue);
                }
                return fieldValues;
            } else {
                return BeanUtil.getFieldValue(property, propertyAttribute);
            }
        }
    }

    @Override
    public Object reverseConvert(BoundedContext boundedContext, Object property) {
        return property;
    }
    
}
