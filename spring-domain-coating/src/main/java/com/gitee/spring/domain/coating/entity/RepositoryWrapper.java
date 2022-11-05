package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.coating.entity.definition.RepositoryDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryWrapper {
    private RepositoryDefinition repositoryDefinition;
    private List<PropertyWrapper> collectedPropertyWrappers;
}
