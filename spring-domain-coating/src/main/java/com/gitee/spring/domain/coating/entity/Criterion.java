package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {
    private ConfiguredRepository configuredRepository;
    private Object example;
}
