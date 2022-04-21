package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.repository.AbstractDelegateRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityPropertyLocation {
    private List<String> multiAccessPath;
    private String prefixAccessPath;
    private boolean forwardParent;
    private String parentAccessPath;
    private ConfiguredRepository parentConfiguredRepository;
    private AbstractDelegateRepository<?, ?> abstractDelegateRepository;
    private EntityPropertyChain entityPropertyChain;
    private ConfiguredRepository belongConfiguredRepository;
}
