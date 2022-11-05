package com.gitee.spring.domain.coating3.entity;

import com.gitee.spring.domain.coating3.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.core3.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyWrapper {
    private Property property;
    private PropertyDefinition propertyDefinition;
}
