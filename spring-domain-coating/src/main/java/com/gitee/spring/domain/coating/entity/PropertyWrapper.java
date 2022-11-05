package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.coating.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.core.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyWrapper {
    private Property property;
    private PropertyDefinition propertyDefinition;
}
