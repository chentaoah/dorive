package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.repository.AbstractDelegateRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryLocation {
    private List<String> multiAccessPath;
    private String parentAccessPath;
    private String prefixAccessPath;
    private String absoluteAccessPath;
    private boolean forwardParent;
    private ConfiguredRepository parentConfiguredRepository;
    private AbstractDelegateRepository<?, ?> abstractDelegateRepository;
    private ConfiguredRepository belongConfiguredRepository;
    private List<PropertyDefinition> collectedPropertyDefinitions;
}
