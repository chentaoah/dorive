package com.gitee.spring.domain.coating3.entity;

import com.gitee.spring.domain.core3.entity.executor.Example;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoCriterion {
    private RepositoryWrapper repositoryWrapper;
    private Example example;
}
