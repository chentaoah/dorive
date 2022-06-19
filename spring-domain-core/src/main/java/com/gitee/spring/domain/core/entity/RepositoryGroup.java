package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryGroup {
    private String accessPath;
    private List<RepositoryDefinition> repositoryDefinitions;
}
