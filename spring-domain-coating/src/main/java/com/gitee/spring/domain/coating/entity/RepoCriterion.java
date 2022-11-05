package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.executor.Example;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoCriterion {
    private RepositoryWrapper repositoryWrapper;
    private Example example;
}
