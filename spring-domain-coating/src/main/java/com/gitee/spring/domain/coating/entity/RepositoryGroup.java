package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.coating.repository.AbstractAwareRepository;
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
