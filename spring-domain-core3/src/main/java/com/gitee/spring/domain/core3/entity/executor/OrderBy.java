package com.gitee.spring.domain.core3.entity.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderBy {
    private String[] columns;
    private String sort;
}
