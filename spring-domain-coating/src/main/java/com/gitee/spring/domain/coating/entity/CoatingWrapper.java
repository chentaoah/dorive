package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.coating.entity.definition.CoatingDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoatingWrapper {
    private CoatingDefinition coatingDefinition;
    private List<RepositoryWrapper> reversedRepositoryWrappers;
}
