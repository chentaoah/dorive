package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityExample;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChainCriterion {
    private RepositoryLocation repositoryLocation;
    private EntityExample entityExample;
}
