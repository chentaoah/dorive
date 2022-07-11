package com.gitee.spring.domain.coating.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryLocation {
    private RepositoryDefinition repositoryDefinition;
    private List<PropertyDefinition> collectedPropertyDefinitions;
}
