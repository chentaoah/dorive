package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CoatingDefinition {
    private Class<?> entityClass;
    private Class<?> coatingClass;
    private AnnotationAttributes attributes;
    private String name;
    private Map<String, PropertyDefinition> propertyDefinitionMap;
    private List<EntityPropertyLocation> reversedEntityPropertyLocations;
}
