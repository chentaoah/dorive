package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.repository.AbstractDelegatedRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityPropertyLocation {
    private List<String> multiAccessPath;
    private String prefixAccessPath;
    private String parentAccessPath;
    private ConfiguredRepository parentConfiguredRepository;
    private AbstractDelegatedRepository<?, ?> abstractDelegatedRepository;
    private EntityPropertyChain entityPropertyChain;
    private ConfiguredRepository belongConfiguredRepository;
}
