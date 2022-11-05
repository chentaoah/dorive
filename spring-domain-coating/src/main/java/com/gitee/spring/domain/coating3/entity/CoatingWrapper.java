package com.gitee.spring.domain.coating3.entity;

import com.gitee.spring.domain.coating3.entity.definition.CoatingDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoatingWrapper {
    private CoatingDefinition coatingDefinition;
    private List<RepositoryWrapper> reversedRepositoryWrappers;
}
