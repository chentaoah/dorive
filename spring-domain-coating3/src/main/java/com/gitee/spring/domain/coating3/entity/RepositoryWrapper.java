package com.gitee.spring.domain.coating3.entity;

import com.gitee.spring.domain.coating3.entity.definition.RepositoryDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryWrapper {
    private RepositoryDefinition repositoryDefinition;
    private List<PropertyWrapper> collectedPropertyWrappers;
}
