package com.gitee.spring.domain.coating.entity.definition;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryDefinition {
    private String prefixAccessPath;
    private String absoluteAccessPath;
    private boolean delegateRoot;
    private ConfiguredRepository definitionRepository;
    private ConfiguredRepository configuredRepository;
}
