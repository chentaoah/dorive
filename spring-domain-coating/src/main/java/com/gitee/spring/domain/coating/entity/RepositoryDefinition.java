package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryDefinition {
    private String prefixAccessPath;
    private String absoluteAccessPath;
    private boolean aggregateRoot;
    private ConfiguredRepository definitionRepository;
    private ConfiguredRepository configuredRepository;
}
