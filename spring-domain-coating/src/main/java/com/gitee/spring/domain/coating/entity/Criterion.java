package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {
    private String definitionAccessPath;
    private ConfiguredRepository definitionRepository;
    private ConfiguredRepository queryRepository;
    private Object example;
    private boolean dirtyExample;
    private boolean emptyQuery;
}
