package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.repository.AbstractAwareRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryGroup {
    private String accessPath;
    private AbstractAwareRepository<?, ?> abstractAwareRepository;
    private List<RepositoryDefinition> repositoryDefinitions;
}
