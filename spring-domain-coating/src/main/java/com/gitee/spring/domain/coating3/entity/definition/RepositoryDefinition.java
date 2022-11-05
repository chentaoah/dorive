package com.gitee.spring.domain.coating3.entity.definition;

import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
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
